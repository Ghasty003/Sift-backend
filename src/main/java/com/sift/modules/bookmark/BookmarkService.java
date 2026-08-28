package com.sift.modules.bookmark;

import com.sift.modules.collection.CollectionEntity;
import com.sift.modules.collection.CollectionRepository;
import com.sift.modules.tweet.TweetEntity;
import com.sift.modules.tweet.TweetRepository;
import com.sift.modules.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookmarkService {

    private final CollectionRepository collectionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final TweetRepository tweetRepository;

    public BookmarkService(
            CollectionRepository collectionRepository, BookmarkRepository bookmarkRepository,
            TweetRepository tweetRepository
    ) {
        this.collectionRepository = collectionRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.tweetRepository = tweetRepository;
    }

    @Transactional
    public BookmarkResponseDTO createBookmark(
            Authentication authentication,
            CreateBookmarkRequestDTO request
    ) {

        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        // Find the tweet if Sift has already stored it
        TweetEntity tweet = tweetRepository
                .findByTweetId(request.tweetId())
                .orElseGet(() -> createTweet(request));

        // Prevent duplicate bookmarks
        assert user != null;
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

        // NULL collection = Inbox
        bookmark.setCollection(null);

        BookmarkEntity savedBookmark =
                bookmarkRepository.save(bookmark);

        return toResponse(savedBookmark);
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

    public void addBookmarkToCollection(
            Authentication authentication,
            UUID bookmarkId,
            UUID collectionId
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark =
                bookmarkRepository.findByIdAndUser(bookmarkId, user)
                        .orElseThrow(() ->
                                new RuntimeException("Bookmark not found")
                        );

        CollectionEntity collection =
                collectionRepository.findByIdAndUser(collectionId, user)
                        .orElseThrow(() ->
                                new RuntimeException("Collection not found")
                        );

        bookmark.setCollection(collection);

        bookmarkRepository.save(bookmark);
    }

    private BookmarkResponseDTO toResponse(
            BookmarkEntity bookmark
    ) {

        TweetEntity tweet = bookmark.getTweet();

        return new BookmarkResponseDTO(
                bookmark.getId(),
                tweet.getId(),
                tweet.getUrl(),
                tweet.getAuthorUsername(),
                tweet.getAuthorName(),
                tweet.getText(),
                tweet.getCreatedAt(),
                bookmark.isFavorite(),
                bookmark.isRead(),
                bookmark.getSavedAt()
        );
    }
}
