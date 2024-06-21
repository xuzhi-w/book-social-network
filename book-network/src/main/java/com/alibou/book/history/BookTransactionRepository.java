package com.alibou.book.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookTransactionRepository extends JpaRepository<BookTransactionHistory,Integer> {
    @Query("""
        select history from BookTransactionHistory history
        where history.user.id =: userId
""")
    Page<BookTransactionHistory> findAllBorrowedBooks(Pageable pageable,Integer userId);

    @Query("""
        select history from BookTransactionHistory history
        where history.book.owner.id =: userId
""")
    Page<BookTransactionHistory> findAllReturnBooks(Pageable pageable, Integer userId);

    @Query("""
       select (count(*)>0) as isBorrowed 
       from BookTransactionHistory history
       where history.user.id = :userId
       and history.book.id =: bookId
       and history.returnApproved = false 
       
""")
    boolean isAlreadyBorrowedByUser(Integer bookId, Integer userId);
    @Query("""
        select b from BookTransactionHistory b
        where b.book.id =: bookId 
        and b.user.id=: userId
        and b.returned = false 
        and b.returnApproved = false  
    """)
    Optional<BookTransactionHistory> findByBookIdAndUserId(Integer bookId, Integer userId);
    @Query("""
        select b from BookTransactionHistory b 
        where b.book.id = : bookId
        and b.book.owner.id =: ownerId
        and b.returned =:true
        and b.returnApproved =:false
    """)
    Optional<BookTransactionHistory> findByBookIdAndOwnerId(Integer bookId, Integer ownerId);
}
