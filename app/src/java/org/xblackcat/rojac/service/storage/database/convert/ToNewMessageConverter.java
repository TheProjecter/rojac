package org.xblackcat.rojac.service.storage.database.convert;

import org.xblackcat.rojac.data.NewMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ASUS
 */

class ToNewMessageConverter implements IToObjectConverter<NewMessage> {
    public NewMessage convert(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        int parentId = rs.getInt(2);
        int forumId = rs.getInt(3);
        String subject = rs.getString(4);
        String message = rs.getString(5);
        boolean draft = rs.getBoolean(6);

        return new NewMessage(id, parentId, forumId, subject, message, draft);
    }
}
