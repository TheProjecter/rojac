package org.xblackcat.rojac.service.storage.database;

import org.xblackcat.rojac.data.Mark;
import org.xblackcat.rojac.data.NewRating;
import org.xblackcat.rojac.service.storage.INewRatingAH;
import org.xblackcat.rojac.service.storage.StorageException;
import org.xblackcat.rojac.service.storage.database.convert.Converters;

import java.util.Collection;

/**
 * @author ASUS
 */

final class DBNewRatingAH extends AnAH implements INewRatingAH {
    DBNewRatingAH(IQueryExecutor helper) {
        super(helper);
    }

    public void storeNewRating(int messageId, Mark rate) throws StorageException {
        long nextId = helper.executeSingle(
                Converters.TO_NUMBER,
                DataQuery.GET_NEXT_ID_NEW_RATING
        ).longValue();
        helper.update(
                DataQuery.STORE_OBJECT_NEW_RATING,
                nextId,
                messageId,
                rate.getValue()
        );
    }

    public boolean removeNewRating(int id) throws StorageException {
        return helper.update(DataQuery.REMOVE_OBJECT_NEW_RATING, id) > 0;
    }

    public NewRating[] getNewRatingsByMessageId(int messageId) throws StorageException {
        Collection<NewRating> rates = helper.execute(
                Converters.TO_NEW_RATING,
                DataQuery.GET_OBJECTS_NEW_RATING_BY_MESSAGE_ID,
                messageId);
        return rates.toArray(new NewRating[rates.size()]);
    }

    public NewRating[] getAllNewRatings() throws StorageException {
        Collection<NewRating> newRatings = helper.execute(Converters.TO_NEW_RATING, DataQuery.GET_OBJECTS_NEW_RATING);
        return newRatings.toArray(new NewRating[newRatings.size()]);
    }

    public void clearRatings() throws StorageException {
        helper.update(DataQuery.REMOVE_ALL_OBJECTS_NEW_RATING);
    }

    public Mark[] getNewRatingMarksByMessageId(int messageId) throws StorageException {
        Collection<Mark> rating = helper.execute(
                Converters.TO_MARK,
                DataQuery.GET_OBJECTS_NEW_RATING_MARK_BY_MESSAGE_ID,
                messageId
        );
        return rating.toArray(new Mark[rating.size()]);
    }
}
