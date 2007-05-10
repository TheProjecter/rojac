package org.xblackcat.sunaj.service.data;

/**
 * Date: 14.04.2007
 *
 * @author ASUS
 */

public final class NewRating {
    private final int id;
    private final int messageId;
    private final int rate;

    public NewRating(int id, int messageId, int rate) {
        this.id = id;
        this.messageId = messageId;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getRate() {
        return rate;
    }

    public String toString() {
        StringBuilder str = new StringBuilder("NewRating[");
        str.append("id=").append(id).append(", ");
        str.append("messageId=").append(messageId).append(", ");
        str.append("rate=").append(rate).append(']');
        return str.toString();
    }
}
