package models;

public class Review {
    private int bookingId;
    private int star;
    private String title;
    private String content;

    //---------------Start Constructor----------------
    public Review(int bookingId, int star, String title, String content) {
        this.bookingId = bookingId;
        this.star = star;
        this.title = title;
        this.content = content;
    }
    //---------------End Constructor----------------

    //---------------Start Getters----------------
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
    //---------------End Getters----------------

    //---------------Start Setters----------------
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
    //---------------End Setters----------------

    @Override
    public String toString() {
        return "Review{" +
                "bookingId=" + bookingId +
                ", star=" + star +
                ", title='" + title + '\'' +
                '}';
    }
}