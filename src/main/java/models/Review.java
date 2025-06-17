package models;

public class Review {
    private int bookingId; // PRIMARY KEY, Foreign Key to bookings.id
    private int star;      // NOT NULL
    private String title;  // NOT NULL
    private String content; // NOT NULL

    public Review(int bookingId, int star, String title, String content) {
        this.bookingId = bookingId;
        this.star = star;
        this.title = title;
        this.content = content;
    }

    // Getters
    public int getBookingId() {
        return bookingId;
    }

    public int getStar() {
        return star;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    // Setters
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Review{" +
                "bookingId=" + bookingId +
                ", star=" + star +
                ", title='" + title + '\'' +
                '}';
    }
}
