import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Exception Hierarchy
class LibraryException extends Exception {
    String code;
    LibraryException(String msg) { super(msg); }
    LibraryException(String msg, Throwable cause) { super(msg, cause); }
    LibraryException(String msg, String code) { super(msg); this.code = code; }
    String getCode() { return code; }
}

class BookNotFoundException extends LibraryException {
    BookNotFoundException(String id) { super("Book not found: " + id, "LIB-001"); }
}
class BookCheckedOutException extends LibraryException {
    BookCheckedOutException(String id) { super("Book checked out: " + id, "LIB-002"); }
}
class CardExpiredException extends LibraryException {
    CardExpiredException(String id) { super("Card expired: " + id, "LIB-003"); }
}
class UnpaidFinesException extends LibraryException {
    UnpaidFinesException(String id, double amt) { super("Unpaid fines $" + amt + " for: " + id, "LIB-004"); }
}
class DatabaseException extends RuntimeException {
    DatabaseException(String msg) { super(msg); }
}
class BookDamagedException extends LibraryException {
    BookDamagedException(String id) { super("Book damaged: " + id, "LIB-005"); }
}

// Model Classes
class Book {
    String id; int copies; boolean damaged;
    Book(String id, int copies) { this.id = id; this.copies = copies; }
}

class Member {
    String id; LocalDate expiry; double fines; List<String> borrowed = new ArrayList<>();
    Member(String id, LocalDate expiry) { this.id = id; this.expiry = expiry; }
    boolean canBorrow() { 
        return !LocalDate.now().isAfter(expiry) && fines < 10 && borrowed.size() < 5;
    }
}

class Loan {
    String id, bookId, memberId; LocalDate due; boolean returned; double fine;
    Loan(String id, String bookId, String memberId, LocalDate due) {
        this.id = id; this.bookId = bookId; this.memberId = memberId; this.due = due;
    }
}

// Service Class
class LibraryService {
    Map<String, Book> books = new ConcurrentHashMap<>();
    Map<String, Member> members = new HashMap<>();
    Map<String, Loan> loans = new HashMap<>();
    int counter = 0;
    
    void addBook(Book b) { books.put(b.id, b); }
    void addMember(Member m) { members.put(m.id, m); }
    
    Loan borrowBook(String bookId, String memberId) throws LibraryException {
        Book book = books.get(bookId);
        if (book == null) throw new BookNotFoundException(bookId);
        if (book.damaged) throw new BookDamagedException(bookId);
        if (book.copies <= 0) throw new BookCheckedOutException(bookId);
        
        Member member = members.get(memberId);
        if (member == null) throw new CardExpiredException(memberId);
        if (!member.canBorrow()) {
            if (LocalDate.now().isAfter(member.expiry)) throw new CardExpiredException(memberId);
            if (member.fines >= 10) throw new UnpaidFinesException(memberId, member.fines);
        }
        
        try {
            String loanId = "L-" + (++counter);
            Loan loan = new Loan(loanId, bookId, memberId, LocalDate.now().plusDays(14));
            book.copies--;
            member.borrowed.add(bookId);
            loans.put(loanId, loan);
            return loan;
        } catch (Exception e) {
            throw new LibraryException("Loan failed", e);
        }
    }
    
    double returnBook(String loanId) throws LibraryException {
        try (LoanProcessor p = new LoanProcessor()) {
            Loan loan = loans.get(loanId);
            if (loan == null) throw new LibraryException("Loan not found", "LIB-006");
            if (loan.returned) throw new LibraryException("Already returned", "LIB-007");
            
            loan.returned = true;
            loan.fine = LocalDate.now().isAfter(loan.due) ? 
                       LocalDate.now().until(loan.due).getDays() * 0.5 : 0;
            
            Book book = books.get(loan.bookId);
            if (book != null) book.copies++;
            
            Member member = members.get(loan.memberId);
            if (member != null) {
                member.borrowed.remove(loan.bookId);
                member.fines += loan.fine;
            }
            return loan.fine;
        }
    }
    
    static class LoanProcessor implements AutoCloseable {
        public void close() { /* cleanup */ }
    }
}

// Main Application
public class LibrarySystem {
    public static void main(String[] args) throws LibraryException {
        LibraryService lib = new LibraryService();
        
        // Setup
        lib.addBook(new Book("B1", 3));
        lib.addBook(new Book("B2", 2));
        lib.addMember(new Member("M1", LocalDate.now().plusMonths(6)));
        lib.addMember(new Member("M2", LocalDate.now().minusMonths(1)));
        
        // Test 1: Success
        System.out.println("Test 1: Success");
        try {
            Loan loan = lib.borrowBook("B1", "M1");
            System.out.println("Borrowed: " + loan.bookId + " Due: " + loan.due);
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Test 2: Book Not Found
        System.out.println("\nTest 2: Book Not Found");
        try {
            lib.borrowBook("B999", "M1");
        } catch (BookNotFoundException e) {
            System.out.println("Caught: " + e.getMessage() + " [Code: " + e.getCode() + "]");
        }
        
        // Test 3: Card Expired
        System.out.println("\nTest 3: Card Expired");
        try {
            lib.borrowBook("B2", "M2");
        } catch (CardExpiredException e) {
            System.out.println("Caught: " + e.getMessage() + " [Code: " + e.getCode() + "]");
        }
        
        // Test 4: Already Checked Out
        System.out.println("\nTest 4: Already Checked Out");
        try {
            lib.borrowBook("B1", "M1");
            lib.borrowBook("B1", "M1");
        } catch (BookCheckedOutException e) {
            System.out.println("Caught: " + e.getMessage() + " [Code: " + e.getCode() + "]");
        }
        
        // Test 5: Exception Chaining
        System.out.println("\nTest 5: Exception Chaining");
        try {
            simulateError(lib);
        } catch (LibraryException e) {
            System.out.println("Caught: " + e.getMessage());
            if (e.getCause() != null)
                System.out.println("Cause: " + e.getCause().getMessage());
        }
        
        // Test 6: Finally Block
        System.out.println("\nTest 6: Finally Block");
        try {
            lib.returnBook("L-1");
        } catch (LibraryException e) {
            System.out.println("Exception: " + e.getMessage());
        } finally {
            System.out.println("Finally executed!");
        }
    }
    
    static void simulateError(LibraryService lib) throws LibraryException {
        try {
            throw new DatabaseException("DB connection lost");
        } catch (DatabaseException e) {
            throw new LibraryException("Database error occurred", e);
        }
    }
}