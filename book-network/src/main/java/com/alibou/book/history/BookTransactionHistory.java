package com.alibou.book.history;

import com.alibou.book.book.Book;
import com.alibou.book.common.BaseEntity;
import com.alibou.book.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BookTransactionHistory extends BaseEntity {

    //user relationship
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    //book relationship
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;


    private boolean returned;
    private boolean returnApproved;

}
