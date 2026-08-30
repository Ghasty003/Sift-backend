package com.sift.modules.bookmark;

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
    public List<BookmarkResponseDTO> getBookmarks(
            Authentication authentication
    ) {

        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        // 1. Fetch the user's bookmarks.
        List<BookmarkEntity> bookmarks =
                bookmarkRepository
                        .findAllByUserIdOrderBySavedAtDesc(
                                user.getId()
                        );

        if (bookmarks.isEmpty()) {
            return List.of();
        }

        // 2. Extract bookmark IDs.
        List<UUID> bookmarkIds =
                bookmarks.stream()
                        .map(BookmarkEntity::getId)
                        .toList();

        // 3. Fetch all tags for these bookmarks in one query.
        List<BookmarkTagEntity> bookmarkTags =
                bookmarkTagRepository
                        .findAllByBookmarkIdIn(bookmarkIds);

        // 4. Group tags by bookmark ID.
        Map<UUID, List<TagResponseDTO>> tagsByBookmark =
                bookmarkTags.stream()
                        .collect(Collectors.groupingBy(
                                BookmarkTagEntity::getBookmarkId,
                                Collectors.mapping(
                                        bookmarkTag ->
                                                new TagResponseDTO(
                                                        bookmarkTag
                                                                .getTag()
                                                                .getId(),
                                                        bookmarkTag
                                                                .getTag()
                                                                .getName()
                                                ),
                                        Collectors.toList()
                                )
                        ));

        // 5. Fetch all notes in one query.
        List<BookmarkNoteEntity> notes =
                bookmarkNoteRepository
                        .findAllByBookmarkIdIn(bookmarkIds);

        // 6. Convert notes into:
        // bookmarkId -> note
        Map<UUID, BookmarkNoteEntity> notesByBookmark =
                notes.stream()
                        .collect(Collectors.toMap(
                                note ->
                                        note.getBookmark()
                                                .getId(),
                                Function.identity()
                        ));

        // 7. Build the final response.
        return bookmarks.stream()
                .map(bookmark -> {

                    List<TagResponseDTO> tags =
                            tagsByBookmark.getOrDefault(
                                    bookmark.getId(),
                                    List.of()
                            );

                    BookmarkNoteEntity note =
                            notesByBookmark.get(
                                    bookmark.getId()
                            );

                    NoteResponseDTO noteResponse =
                            note == null
                                    ? null
                                    : new NoteResponseDTO(
                                    note.getId(),
                                    bookmark.getId(),
                                    note.getContent(),
                                    note.getCreatedAt(),
                                    note.getUpdatedAt()
                            );

                    return toResponseDTO(
                            bookmark,
                            tags,
                            noteResponse
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getInboxBookmarks(
            Authentication authentication
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        List<BookmarkEntity> bookmarks =
                bookmarkRepository
                        .findAllByUserIdAndCollectionIsNullOrderBySavedAtDesc(
                                user.getId()
                        );

        return buildBookmarkResponses(bookmarks);
    }


    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getFavoriteBookmarks(
            Authentication authentication
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        List<BookmarkEntity> bookmarks =
                bookmarkRepository
                        .findAllByUserIdAndFavoriteTrueOrderBySavedAtDesc(
                                user.getId()
                        );

        return buildBookmarkResponses(bookmarks);
    }


    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getUnreadBookmarks(
            Authentication authentication
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        List<BookmarkEntity> bookmarks =
                bookmarkRepository
                        .findAllByUserIdAndReadFalseOrderBySavedAtDesc(
                                user.getId()
                        );

        return buildBookmarkResponses(bookmarks);
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getBookmarksInCollection(
            Authentication authentication,
            UUID collectionId
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        // Make sure this collection belongs to the authenticated user.
        CollectionEntity collection =
                collectionRepository
                        .findByIdAndUser(collectionId, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Collection not found"
                                )
                        );

        // Fetch only this user's bookmarks in this collection.
        List<BookmarkEntity> bookmarks =
                bookmarkRepository
                        .findAllByUserAndCollectionOrderBySavedAtDesc(
                                user,
                                collection
                        );

        if (bookmarks.isEmpty()) {
            return List.of();
        }

        List<UUID> bookmarkIds =
                bookmarks.stream()
                        .map(BookmarkEntity::getId)
                        .toList();

        // Fetch all tags in one query.
        List<BookmarkTagEntity> bookmarkTags =
                bookmarkTagRepository
                        .findAllByBookmarkIdIn(bookmarkIds);

        Map<UUID, List<TagResponseDTO>> tagsByBookmark =
                bookmarkTags.stream()
                        .collect(Collectors.groupingBy(
                                BookmarkTagEntity::getBookmarkId,
                                Collectors.mapping(
                                        bookmarkTag ->
                                                new TagResponseDTO(
                                                        bookmarkTag
                                                                .getTag()
                                                                .getId(),
                                                        bookmarkTag
                                                                .getTag()
                                                                .getName()
                                                ),
                                        Collectors.toList()
                                )
                        ));

        // Fetch all notes in one query.
        List<BookmarkNoteEntity> notes =
                bookmarkNoteRepository
                        .findAllByBookmarkIdIn(bookmarkIds);

        Map<UUID, BookmarkNoteEntity> notesByBookmark =
                notes.stream()
                        .collect(Collectors.toMap(
                                note ->
                                        note.getBookmark()
                                                .getId(),
                                Function.identity()
                        ));

        return bookmarks.stream()
                .map(bookmark -> {

                    List<TagResponseDTO> tags =
                            tagsByBookmark.getOrDefault(
                                    bookmark.getId(),
                                    List.of()
                            );

                    BookmarkNoteEntity note =
                            notesByBookmark.get(
                                    bookmark.getId()
                            );

                    NoteResponseDTO noteResponse =
                            note == null
                                    ? null
                                    : new NoteResponseDTO(
                                    note.getId(),
                                    bookmark.getId(),
                                    note.getContent(),
                                    note.getCreatedAt(),
                                    note.getUpdatedAt()
                            );

                    return new BookmarkResponseDTO(
                            bookmark.getId(),

                            new TweetResponseDTO(
                                    bookmark.getTweet().getId(),
                                    bookmark.getTweet().getUrl(),
                                    bookmark.getTweet().getAuthorUsername(),
                                    bookmark.getTweet().getAuthorName(),
                                    bookmark.getTweet().getText(),
                                    bookmark.getTweet().getCreatedAt()
                            ),

                            new CollectionResponseDTO(
                                    bookmark.getCollection().getId(),
                                    bookmark.getCollection().getName(),
                                    bookmark.getCollection().getDescription(),
                                    bookmark.getCollection().getCreatedAt(),
                                    bookmark.getCollection().getUpdatedAt()
                            ),

                            bookmark.isFavorite(),
                            bookmark.isRead(),
                            bookmark.getSavedAt(),
                            tags,
                            noteResponse
                    );
                })
                .toList();
    }

    @Transactional
    public BookmarkResponseDTO toggleFavorite(
            Authentication authentication,
            UUID bookmarkId
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark =
                bookmarkRepository
                        .findByIdAndUser(bookmarkId, user)
                        .orElseThrow(() ->
                                new RuntimeException("Bookmark not found")
                        );

        bookmark.setFavorite(!bookmark.isFavorite());

        BookmarkEntity saved =
                bookmarkRepository.save(bookmark);

        return toResponseDTO(saved);
    }


    @Transactional
    public BookmarkResponseDTO toggleRead(
            Authentication authentication,
            UUID bookmarkId
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark =
                bookmarkRepository
                        .findByIdAndUser(bookmarkId, user)
                        .orElseThrow(() ->
                                new RuntimeException("Bookmark not found")
                        );

        bookmark.setRead(!bookmark.isRead());

        BookmarkEntity saved =
                bookmarkRepository.save(bookmark);

        return toResponseDTO(saved);
    }


    @Transactional
    public void deleteBookmark(
            Authentication authentication,
            UUID bookmarkId
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark =
                bookmarkRepository
                        .findByIdAndUser(bookmarkId, user)
                        .orElseThrow(() ->
                                new RuntimeException("Bookmark not found")
                        );

        bookmarkRepository.delete(bookmark);
    }

    @Transactional
    public BookmarkResponseDTO createBookmark(
            Authentication authentication,
            CreateBookmarkRequestDTO request
    ) {

        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        // Find the tweet if Sift has already stored it.
        TweetEntity tweet =
                tweetRepository
                        .findByTweetId(request.tweetId())
                        .orElseGet(() -> createTweet(request));

        // Prevent duplicate bookmarks.
        if (bookmarkRepository.existsByUser_IdAndTweet_Id(
                user.getId(),
                tweet.getId()
        )) {
            throw new IllegalStateException(
                    "Tweet has already been bookmarked"
            );
        }

        BookmarkEntity bookmark = new BookmarkEntity();

        bookmark.setUser(user);
        bookmark.setTweet(tweet);

        // NULL collection = Inbox.
        bookmark.setCollection(null);

        BookmarkEntity savedBookmark =
                bookmarkRepository.save(bookmark);

        return toResponseDTO(savedBookmark);
    }

    private TweetEntity createTweet(
            CreateBookmarkRequestDTO request
    ) {

        TweetEntity tweet = new TweetEntity();

        tweet.setTweetId(request.tweetId());
        tweet.setUrl(request.url());
        tweet.setAuthorUsername(request.authorUsername());
        tweet.setAuthorName(request.authorName());
        tweet.setText(request.text());
        tweet.setCreatedAt(request.createdAt());

        return tweetRepository.save(tweet);
    }

    @Transactional
    public void addBookmarkToCollection(
            Authentication authentication,
            UUID bookmarkId,
            UUID collectionId
    ) {

        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark =
                bookmarkRepository
                        .findByIdAndUser(bookmarkId, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bookmark not found"
                                )
                        );

        CollectionEntity collection =
                collectionRepository
                        .findByIdAndUser(collectionId, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Collection not found"
                                )
                        );

        bookmark.setCollection(collection);

        bookmarkRepository.save(bookmark);
    }

    @Transactional(readOnly = true)
    public BookmarkSummaryResponseDTO getBookmarkSummary(
            Authentication authentication
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        long inboxCount =
                bookmarkRepository.countByUser_IdAndCollectionIsNull(
                        user.getId()
                );

        long favoriteCount =
                bookmarkRepository.countByUser_IdAndFavoriteTrue(
                        user.getId()
                );

        List<BookmarkEntity> recentBookmarks =
                bookmarkRepository
                        .findTop5ByUser_IdOrderBySavedAtDesc(
                                user.getId()
                        );

        List<BookmarkResponseDTO> recentBookmarkDTOs =
                recentBookmarks.stream()
                        .map(this::toResponseDTO)
                        .toList();

        return new BookmarkSummaryResponseDTO(
                inboxCount,
                favoriteCount,
                recentBookmarkDTOs
        );
    }

    private BookmarkResponseDTO toResponseDTO(
            BookmarkEntity bookmark
    ) {

        TweetEntity tweet =
                bookmark.getTweet();

        TweetResponseDTO tweetDTO =
                new TweetResponseDTO(
                        tweet.getId(),
                        tweet.getUrl(),
                        tweet.getAuthorUsername(),
                        tweet.getAuthorName(),
                        tweet.getText(),
                        tweet.getCreatedAt()
                );

        CollectionResponseDTO collectionDTO = null;

        if (bookmark.getCollection() != null) {

            CollectionEntity collection =
                    bookmark.getCollection();

            collectionDTO =
                    new CollectionResponseDTO(
                            collection.getId(),
                            collection.getName(),
                            collection.getDescription(),
                            collection.getCreatedAt(),
                            collection.getUpdatedAt()
                    );
        }

        List<TagResponseDTO> tagDTOs =
                bookmark.getTags()
                        .stream()
                        .map(tag ->
                                new TagResponseDTO(
                                        tag.getId(),
                                        tag.getName()
                                )
                        )
                        .toList();

        NoteResponseDTO noteDTO = null;

        if (bookmark.getNote() != null) {

            noteDTO =
                    new NoteResponseDTO(
                            bookmark.getNote().getId(),
                            bookmark.getId(),
                            bookmark.getNote().getContent(),
                            bookmark.getNote().getCreatedAt(),
                            bookmark.getNote().getUpdatedAt()
                    );
        }

        return new BookmarkResponseDTO(
                bookmark.getId(),
                tweetDTO,
                collectionDTO,
                bookmark.isFavorite(),
                bookmark.isRead(),
                bookmark.getSavedAt(),
                tagDTOs,
                noteDTO
        );
    }

    private BookmarkResponseDTO toResponseDTO(
            BookmarkEntity bookmark,
            List<TagResponseDTO> tags,
            NoteResponseDTO note
    ) {

        TweetEntity tweet =
                bookmark.getTweet();

        TweetResponseDTO tweetDTO =
                new TweetResponseDTO(
                        tweet.getId(),
                        tweet.getUrl(),
                        tweet.getAuthorUsername(),
                        tweet.getAuthorName(),
                        tweet.getText(),
                        tweet.getCreatedAt()
                );

        CollectionResponseDTO collectionDTO = null;

        if (bookmark.getCollection() != null) {

            CollectionEntity collection =
                    bookmark.getCollection();

            collectionDTO =
                    new CollectionResponseDTO(
                            collection.getId(),
                            collection.getName(),
                            collection.getDescription(),
                            collection.getCreatedAt(),
                            collection.getUpdatedAt()
                    );
        }

        return new BookmarkResponseDTO(
                bookmark.getId(),
                tweetDTO,
                collectionDTO,
                bookmark.isFavorite(),
                bookmark.isRead(),
                bookmark.getSavedAt(),
                tags,
                note
        );
    }

    private List<BookmarkResponseDTO> buildBookmarkResponses(
            List<BookmarkEntity> bookmarks
    ) {
        if (bookmarks.isEmpty()) {
            return List.of();
        }

        List<UUID> bookmarkIds =
                bookmarks.stream()
                        .map(BookmarkEntity::getId)
                        .toList();

        // Fetch all tags in one query.
        List<BookmarkTagEntity> bookmarkTags =
                bookmarkTagRepository
                        .findAllByBookmarkIdIn(bookmarkIds);

        Map<UUID, List<TagResponseDTO>> tagsByBookmark =
                bookmarkTags.stream()
                        .collect(Collectors.groupingBy(
                                BookmarkTagEntity::getBookmarkId,
                                Collectors.mapping(
                                        bookmarkTag ->
                                                new TagResponseDTO(
                                                        bookmarkTag.getTag().getId(),
                                                        bookmarkTag.getTag().getName()
                                                ),
                                        Collectors.toList()
                                )
                        ));

        // Fetch all notes in one query.
        List<BookmarkNoteEntity> notes =
                bookmarkNoteRepository
                        .findAllByBookmarkIdIn(bookmarkIds);

        Map<UUID, BookmarkNoteEntity> notesByBookmark =
                notes.stream()
                        .collect(Collectors.toMap(
                                note -> note.getBookmark().getId(),
                                Function.identity()
                        ));

        return bookmarks.stream()
                .map(bookmark -> {

                    List<TagResponseDTO> tags =
                            tagsByBookmark.getOrDefault(
                                    bookmark.getId(),
                                    List.of()
                            );

                    BookmarkNoteEntity note =
                            notesByBookmark.get(bookmark.getId());

                    NoteResponseDTO noteResponse =
                            note == null
                                    ? null
                                    : new NoteResponseDTO(
                                    note.getId(),
                                    bookmark.getId(),
                                    note.getContent(),
                                    note.getCreatedAt(),
                                    note.getUpdatedAt()
                            );

                    TweetEntity tweet = bookmark.getTweet();

                    TweetResponseDTO tweetDTO =
                            new TweetResponseDTO(
                                    tweet.getId(),
                                    tweet.getUrl(),
                                    tweet.getAuthorUsername(),
                                    tweet.getAuthorName(),
                                    tweet.getText(),
                                    tweet.getCreatedAt()
                            );

                    CollectionResponseDTO collectionDTO = null;

                    if (bookmark.getCollection() != null) {
                        CollectionEntity collection =
                                bookmark.getCollection();

                        collectionDTO =
                                new CollectionResponseDTO(
                                        collection.getId(),
                                        collection.getName(),
                                        collection.getDescription(),
                                        collection.getCreatedAt(),
                                        collection.getUpdatedAt()
                                );
                    }

                    return new BookmarkResponseDTO(
                            bookmark.getId(),
                            tweetDTO,
                            collectionDTO,
                            bookmark.isFavorite(),
                            bookmark.isRead(),
                            bookmark.getSavedAt(),
                            tags,
                            noteResponse
                    );
                })
                .toList();
    }

}