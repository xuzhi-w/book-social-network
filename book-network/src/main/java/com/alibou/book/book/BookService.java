package com.alibou.book.book;

import com.alibou.book.common.PageResponse;
import com.alibou.book.exception.OperationNotPermiitedException;
import com.alibou.book.file.FileStorageService;
import com.alibou.book.history.BookTransactionHistory;
import com.alibou.book.history.BookTransactionRepository;
import com.alibou.book.user.User;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.OperationNotSupportedException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistory bookTransactionHistory;
    private final BookTransactionRepository bookTransactionRepository;
    private final FileStorageService fileStorageService;
    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = (User)connectedUser.getPrincipal();
        Book book = bookMapper.toBook(request);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the ID : " + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser){
        User user = (User)connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable,user.getId());
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
        return new PageResponse<BookResponse>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User user = (User)connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(user.getId()),pageable);
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
        return new PageResponse<BookResponse>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size,
                                                                   Authentication connectedUser) {
        User user = (User)connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page,size,Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionRepository
                .findAllBorrowedBooks(pageable,user.getId());
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .collect(Collectors.toList());
        return new PageResponse<BorrowedBookResponse>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size,
                                                                   Authentication connectedUser) {
        User user = (User)connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page,size,Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allReturnedBooks = bookTransactionRepository
                .findAllReturnBooks(pageable,user.getId());
        List<BorrowedBookResponse> bookResponse = allReturnedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .collect(Collectors.toList());
        return new PageResponse<BorrowedBookResponse>(
                bookResponse,
                allReturnedBooks.getNumber(),
                allReturnedBooks.getSize(),
                allReturnedBooks.getTotalElements(),
                allReturnedBooks.getTotalPages(),
                allReturnedBooks.isFirst(),
                allReturnedBooks.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book  = bookRepository.findById(bookId).orElseThrow(
                ()->new EntityNotFoundException("No book fould with ID:: " + bookId)
        );
        User user = (User)connectedUser.getPrincipal();
        if(!Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermiitedException("You can not update other's books shareable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book  = bookRepository.findById(bookId).orElseThrow(
                ()->new EntityNotFoundException("No book found with ID:: " + bookId)
        );
        User user = (User)connectedUser.getPrincipal();
        if(!Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermiitedException("You can not update other's books archived status");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book  = bookRepository.findById(bookId).orElseThrow(
                ()->new EntityNotFoundException("No book found with ID:: " + bookId)
        );
        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermiitedException("The requested book can not" +
                    " be borrowed since it is archived or not shareable ");
        }

        User user = (User)connectedUser.getPrincipal();
        if(Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermiitedException("You can not borrow your own books");
        }
        final boolean isAlreadyBorrowed = bookTransactionRepository.isAlreadyBorrowedByUser(bookId,user.getId());
        if(isAlreadyBorrowed){
            throw new OperationNotPermiitedException("The requested book is already borrowed");
        }
        BookTransactionHistory bookTransactionHistory1 = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return bookTransactionRepository.save(bookTransactionHistory1).getId();
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book  = bookRepository.findById(bookId).orElseThrow(
                ()->new EntityNotFoundException("No book found with ID:: " + bookId)
        );

        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermiitedException("The requested book can not" +
                    " be returned since it is archived or not shareable ");
        }

        User user = (User)connectedUser.getPrincipal();
        if(Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermiitedException("You can not return your own books");
        }
        BookTransactionHistory bookTransactionHistory1 = bookTransactionRepository
                .findByBookIdAndUserId(bookId,user.getId()).orElseThrow(
                        ()-> new OperationNotPermiitedException("You did not borrow this book.")
                );
        bookTransactionHistory1.setReturned(true);
        return bookTransactionRepository.save(bookTransactionHistory1).getId();
    }


    public Integer approveReturnedBook(Integer bookId, Authentication connectedUser) {
        Book book  = bookRepository.findById(bookId).orElseThrow(
                ()->new EntityNotFoundException("No book found with ID:: " + bookId)
        );

        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermiitedException("The requested book can not" +
                    " be returned since it is archived or not shareable ");
        }

        User user = (User)connectedUser.getPrincipal();
        if(Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermiitedException("You can not return your own books");
        }
        BookTransactionHistory bookTransactionHistory1 = bookTransactionRepository
                .findByBookIdAndOwnerId(bookId,user.getId()).orElseThrow(
                        ()-> new OperationNotPermiitedException("The book is not returned yet,You can not approve it")
                );
        bookTransactionHistory1.setReturnApproved(true);
        return bookTransactionRepository.save(bookTransactionHistory1).getId();
    }


    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        Book book  = bookRepository.findById(bookId).orElseThrow(
                ()->new EntityNotFoundException("No book found with ID:: " + bookId)
        );
        User user = (User)connectedUser.getPrincipal();
        String bookCover = fileStorageService.saveFile(file,user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
