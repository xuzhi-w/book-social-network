package com.alibou.book.feedback;

import com.alibou.book.book.Book;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.DoubleStream;

@Service
public class FeedbackMapper {
    public Feedback toFeedback(FeedbackRequest request) {
        return Feedback.builder()
                .note(request.note())
                .comment(request.comment())
                .book(Book.builder()
                        .id(request.bookId())
                        .archived(false)
                        .shareable(false)
                        .build()
                )
                .build();
    }

    public FeedbackResponse toFeedbackResponse(Feedback f, Integer userId) {
         return FeedbackResponse.builder()
                 .note(f.getNote())
                 .comment(f.getComment())
                 .ownFeedback((Objects.equals(f.getCreatedBy(),userId)))
                 .build();
    }
}
