package com.sift.modules.bookmark;

import com.sift.common.CursorPageResponseDTO;
import com.sift.exceptions.ConflictException;
import com.sift.exceptions.ResourceNotFoundException;
import com.sift.modules.collection.CollectionEntity;
import com.sift.modules.collection.CollectionRepository;
import com.sift.modules.collection.CollectionResponseDTO;
import com.sift.modules.note.BookmarkNoteEntity;
import com.sift.modules.note.BookmarkNoteRepository;
import com.sift.modules.note.NoteResponseDTO;
import com.sift.modules.tag.BookmarkTagEntity;
import com.sift.modules.tag.BookmarkTagRepository;
import com.sift.modules.tag.TagResponseDTO;
import com.sift.modules.tweet.TweetEntity;
import com.sift.modules.tweet.TweetRepository;
import com.sift.modules.user.UserEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final CollectionRepository collectionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final TweetRepository tweetRepository;
    private final BookmarkTagRepository bookmarkTagRepository;
    private final BookmarkNoteRepository bookmarkNoteRepository;

    public BookmarkService(
            CollectionRepository collectionRepository,
            BookmarkRepository bookmarkRepository,
            TweetRepository tweetRepository,
            BookmarkTagRepository bookmarkTagRepository,
            BookmarkNoteRepository bookmarkNoteRepository
    ) {
        this.collectionRepository = collectionRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.tweetRepository = tweetRepository;
        this.bookmarkTagRepository = bookmarkTagRepository;
        this.bookmarkNoteRepository = bookmarkNoteRepository;
    }

    @Transactional(readOnly = true)
    public CursorPageResponseDTO<BookmarkResponseDTO> searchBookmarks(
            Authentication authentication,
            BookmarkFilterRequest filter
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        boolean inboxOnly = "inbox".equals(filter.collectionId());

        UUID collectionId = null;
        if (filter.collectionId() != null && !inboxOnly) {
            CollectionEntity collection = collectionRepository
                    .findByIdAndUser(UUID.fromString(filter.collectionId()), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
            collectionId = collection.getId();
        }
        boolean hasCollectionId = collectionId != null;

        boolean hasTagId = filter.tagId() != null;
        boolean hasReadFilter = filter.read() != null;
        boolean readFilter = hasReadFilter && filter.read();

        String searchPattern = (filter.search() == null || filter.search().isBlank())
                ? null
                : "%" + filter.search().trim().toLowerCase() + "%";
        boolean hasSearch = searchPattern != null;

        BookmarkCursor cursor = filter.cursor() != null ? BookmarkCursor.decode(filter.cursor()) : null;
        boolean hasCursor = cursor != null;

        int limit = filter.limit() != null
                ? Math.min(Math.max(filter.limit(), 1), MAX_PAGE_SIZE)
                : DEFAULT_PAGE_SIZE;

        boolean ascending = "oldest".equals(filter.sort());

        List<BookmarkEntity> rows = ascending
                ? bookmarkRepository.searchBookmarksAscending(
                user.getId(), inboxOnly, hasCollectionId,
                hasCollectionId ? collectionId : new UUID(0, 0),
                filter.favoriteOnly(), hasReadFilter, readFilter,
                hasTagId, hasTagId ? filter.tagId() : new UUID(0, 0),
                hasSearch, hasSearch ? searchPattern : "",
                hasCursor, hasCursor ? cursor.savedAt() : java.time.OffsetDateTime.now(),
                hasCursor ? cursor.id() : new UUID(0, 0),
                PageRequest.of(0, limit + 1)
        )
                : bookmarkRepository.searchBookmarks(
                user.getId(), inboxOnly, hasCollectionId,
                hasCollectionId ? collectionId : new UUID(0, 0),
                filter.favoriteOnly(), hasReadFilter, readFilter,
                hasTagId, hasTagId ? filter.tagId() : new UUID(0, 0),
                hasSearch, hasSearch ? searchPattern : "",
                hasCursor, hasCursor ? cursor.savedAt() : java.time.OffsetDateTime.now(),
                hasCursor ? cursor.id() : new UUID(0, 0),
                PageRequest.of(0, limit + 1)
        );

        boolean hasMore = rows.size() > limit;
        List<BookmarkEntity> page = hasMore ? rows.subList(0, limit) : rows;

        List<BookmarkResponseDTO> items = buildBookmarkResponses(page);

        String nextCursor = hasMore
                ? new BookmarkCursor(
                page.get(page.size() - 1).getSavedAt(),
                page.get(page.size() - 1).getId()
        ).encode()
                : null;

        return new CursorPageResponseDTO<>(items, nextCursor, hasMore);
    }

    @Transactional
    public BookmarkResponseDTO toggleFavorite(Authentication authentication, UUID bookmarkId) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark = bookmarkRepository
                .findByIdAndUser(bookmarkId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));

        bookmark.setFavorite(!bookmark.isFavorite());
        BookmarkEntity saved = bookmarkRepository.save(bookmark);

        return toResponseDTO(saved);
    }

    @Transactional
    public BookmarkResponseDTO toggleRead(Authentication authentication, UUID bookmarkId) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark = bookmarkRepository
                .findByIdAndUser(bookmarkId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));

        bookmark.setRead(!bookmark.isRead());
        BookmarkEntity saved = bookmarkRepository.save(bookmark);

        return toResponseDTO(saved);
    }

    @Transactional
    public void deleteBookmark(Authentication authentication, UUID bookmarkId) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark = bookmarkRepository
                .findByIdAndUser(bookmarkId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));

        bookmarkRepository.delete(bookmark);
    }

    @Transactional
    public BookmarkResponseDTO createBookmark(Authentication authentication, CreateBookmarkRequestDTO request) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        TweetEntity tweet = tweetRepository
                .findByTweetId(request.tweetId())
                .orElseGet(() -> createTweet(request));

        if (bookmarkRepository.existsByUser_IdAndTweet_Id(user.getId(), tweet.getId())) {
            throw new ConflictException("Tweet has already been bookmarked");
        }

        BookmarkEntity bookmark = new BookmarkEntity();
        bookmark.setUser(user);
        bookmark.setTweet(tweet);
        bookmark.setCollection(null);

        BookmarkEntity savedBookmark = bookmarkRepository.save(bookmark);

        return toResponseDTO(savedBookmark);
    }

    private TweetEntity createTweet(CreateBookmarkRequestDTO request) {
        TweetEntity tweet = new TweetEntity();
        tweet.setTweetId(request.tweetId());
        tweet.setUrl(request.url());
        tweet.setAuthorUsername(request.authorUsername());
        tweet.setAuthorName(request.authorName());
        tweet.setAuthorAvatarUrl(request.authorAvatarUrl());
        tweet.setText(request.text());
        tweet.setCreatedAt(request.createdAt());
        tweet.setReply(request.isReply());
        tweet.setReplyToUsername(request.replyToUsername());
        tweet.setRepostedByName(request.repostedByName());
        tweet.setRepostedByUsername(request.repostedByUsername());

        // Defensive: never link a tweet as quoting itself. This should be
        // impossible once the extension's own dedup logic is correct, but
        // guarding here means a future extraction bug degrades to "no quote
        // card shown" instead of a duplicate-key 500.
        if (request.quotedTweet() != null
                && !request.quotedTweet().tweetId().equals(request.tweetId())) {
            TweetEntity quoted = tweetRepository
                    .findByTweetId(request.quotedTweet().tweetId())
                    .orElseGet(() -> createQuotedTweet(request.quotedTweet()));
            tweet.setQuotedTweet(quoted);
        }

        return tweetRepository.save(tweet);
    }

    private TweetEntity createQuotedTweet(QuotedTweetRequestDTO q) {
        TweetEntity tweet = new TweetEntity();
        tweet.setTweetId(q.tweetId());
        tweet.setUrl(q.url());
        tweet.setAuthorUsername(q.authorUsername());
        tweet.setAuthorName(q.authorName());
        tweet.setAuthorAvatarUrl(q.authorAvatarUrl());
        tweet.setText(q.text());
        tweet.setCreatedAt(q.createdAt());
        // A quoted tweet is stored flat — we don't recurse into whatever
        // *that* tweet might itself be quoting. One level of nesting is
        // what the UI renders; deeper chains just show the immediate quote.
        return tweetRepository.save(tweet);
    }

    @Transactional
    public void addBookmarkToCollection(Authentication authentication, UUID bookmarkId, UUID collectionId) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark = bookmarkRepository
                .findByIdAndUser(bookmarkId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));

        CollectionEntity collection = collectionRepository
                .findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        bookmark.setCollection(collection);
        bookmarkRepository.save(bookmark);
    }

    @Transactional(readOnly = true)
    public BookmarkSummaryResponseDTO getBookmarkSummary(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        long inboxCount = bookmarkRepository.countByUser_IdAndCollectionIsNull(user.getId());
        long favoriteCount = bookmarkRepository.countByUser_IdAndFavoriteTrue(user.getId());

        List<BookmarkEntity> recentBookmarks =
                bookmarkRepository.findTop5ByUser_IdOrderBySavedAtDesc(user.getId());

        List<BookmarkResponseDTO> recentBookmarkDTOs =
                recentBookmarks.stream().map(this::toResponseDTO).toList();

        return new BookmarkSummaryResponseDTO(inboxCount, favoriteCount, recentBookmarkDTOs);
    }

    private BookmarkResponseDTO toResponseDTO(BookmarkEntity bookmark) {
        TweetResponseDTO tweetDTO = toTweetResponseDTO(bookmark.getTweet());

        CollectionResponseDTO collectionDTO = null;
        if (bookmark.getCollection() != null) {
            CollectionEntity c = bookmark.getCollection();
            collectionDTO = new CollectionResponseDTO(
                    c.getId(), c.getName(), c.getDescription(), c.getCreatedAt(), c.getUpdatedAt(), 0, 0
            );
        }

        List<TagResponseDTO> tagDTOs = bookmark.getTags().stream()
                .map(tag -> new TagResponseDTO(tag.getId(), tag.getName(), 0))
                .toList();

        NoteResponseDTO noteDTO = null;
        if (bookmark.getNote() != null) {
            noteDTO = new NoteResponseDTO(
                    bookmark.getNote().getId(), bookmark.getId(),
                    bookmark.getNote().getContent(), bookmark.getNote().getCreatedAt(),
                    bookmark.getNote().getUpdatedAt()
            );
        }

        return new BookmarkResponseDTO(
                bookmark.getId(), tweetDTO, collectionDTO, bookmark.isFavorite(),
                bookmark.isRead(), bookmark.getSavedAt(), tagDTOs, noteDTO
        );
    }

    // Recurses exactly once in practice — createQuotedTweet never sets a
    // quotedTweet on the tweet it creates, so getQuotedTweet() on that
    // result is always null and this terminates.
    private TweetResponseDTO toTweetResponseDTO(TweetEntity tweet) {
        TweetResponseDTO quotedDTO = tweet.getQuotedTweet() != null
                ? toTweetResponseDTO(tweet.getQuotedTweet())
                : null;

        return new TweetResponseDTO(
                tweet.getId(),
                tweet.getUrl(),
                tweet.getAuthorUsername(),
                tweet.getAuthorName(),
                tweet.getAuthorAvatarUrl(),
                tweet.getText(),
                tweet.getCreatedAt(),
                tweet.isReply(),
                tweet.getReplyToUsername(),
                tweet.getRepostedByName(),
                tweet.getRepostedByUsername(),
                quotedDTO
        );
    }

    private List<BookmarkResponseDTO> buildBookmarkResponses(List<BookmarkEntity> bookmarks) {
        if (bookmarks.isEmpty()) {
            return List.of();
        }

        List<UUID> bookmarkIds = bookmarks.stream().map(BookmarkEntity::getId).toList();

        List<BookmarkTagEntity> bookmarkTags =
                bookmarkTagRepository.findAllByBookmarkIdIn(bookmarkIds);

        Map<UUID, List<TagResponseDTO>> tagsByBookmark = bookmarkTags.stream()
                .collect(Collectors.groupingBy(
                        BookmarkTagEntity::getBookmarkId,
                        Collectors.mapping(
                                bt -> new TagResponseDTO(bt.getTag().getId(), bt.getTag().getName(), 0),
                                Collectors.toList()
                        )
                ));

        List<BookmarkNoteEntity> notes = bookmarkNoteRepository.findAllByBookmarkIdIn(bookmarkIds);

        Map<UUID, BookmarkNoteEntity> notesByBookmark = notes.stream()
                .collect(Collectors.toMap(n -> n.getBookmark().getId(), Function.identity()));

        return bookmarks.stream()
                .map(bookmark -> {
                    List<TagResponseDTO> tags =
                            tagsByBookmark.getOrDefault(bookmark.getId(), List.of());

                    BookmarkNoteEntity note = notesByBookmark.get(bookmark.getId());
                    NoteResponseDTO noteResponse = note == null ? null : new NoteResponseDTO(
                            note.getId(), bookmark.getId(), note.getContent(),
                            note.getCreatedAt(), note.getUpdatedAt()
                    );

                    TweetResponseDTO tweetDTO = toTweetResponseDTO(bookmark.getTweet());

                    CollectionResponseDTO collectionDTO = null;
                    if (bookmark.getCollection() != null) {
                        CollectionEntity c = bookmark.getCollection();
                        collectionDTO = new CollectionResponseDTO(
                                c.getId(), c.getName(), c.getDescription(),
                                c.getCreatedAt(), c.getUpdatedAt(), 0, 0
                        );
                    }

                    return new BookmarkResponseDTO(
                            bookmark.getId(), tweetDTO, collectionDTO, bookmark.isFavorite(),
                            bookmark.isRead(), bookmark.getSavedAt(), tags, noteResponse
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public long countAll(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return bookmarkRepository.countByUser_Id(user.getId());
    }

    @Transactional(readOnly = true)
    public long countUnread(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return bookmarkRepository.countByUser_IdAndReadFalse(user.getId());
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getRecentBookmarks(Authentication authentication, int limit) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        List<BookmarkEntity> recent = bookmarkRepository.findTop6ByUser_IdOrderBySavedAtDesc(user.getId());
        return recent.stream().limit(limit).map(this::toResponseDTO).toList();
    }

    @Transactional
    public long markAllAsRead(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return bookmarkRepository.markAllAsRead(user.getId());
    }

    @Transactional
    public int bulkDelete(Authentication authentication, List<UUID> bookmarkIds) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return bookmarkRepository.deleteAllByIdInAndUserId(bookmarkIds, user.getId());
    }

    @Transactional
    public int bulkMove(Authentication authentication, List<UUID> bookmarkIds, UUID collectionId) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
        return bookmarkRepository.moveAllByIdInAndUserId(bookmarkIds, collectionId, user.getId());
    }
}