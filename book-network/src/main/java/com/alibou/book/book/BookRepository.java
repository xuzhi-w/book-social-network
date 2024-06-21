package com.alibou.book.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    @Query("""
        select b from Book b  where 
         b.archived = false and 
         b.shareable = true and 
         b.owner.id !=: userId
    """)
    Page<Book> findAllDisplayableBooks(Pageable pageable, Integer userId);
}