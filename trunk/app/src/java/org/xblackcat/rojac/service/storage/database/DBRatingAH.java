package org.xblackcat.rojac.service.storage.database;

import org.xblackcat.rojac.data.Mark;
import org.xblackcat.rojac.data.MarkStat;
import org.xblackcat.rojac.data.Rating;
import org.xblackcat.rojac.service.storage.IRatingAH;
import org.xblackcat.rojac.service.storage.IResult;
import org.xblackcat.rojac.service.storage.StorageException;
import org.xblackcat.rojac.service.storage.database.convert.Converters;
import ru.rsdn.janus.JanusRatingInfo;

/**
 * @author ASUS
 */

final class DBRatingAH extends AnAH implements IRatingAH {
    DBRatingAH(IQueryHolder helper) {
        super(helper);
    }

    public void storeRating(JanusRatingInfo r) throws StorageException {
        helper.update(DataQuery.STORE_OBJECT_RATING,
                r.getMessageId(),
                r.getTopicId(),
                r.getUserId(),
                r.getUserRating(),
                Mark.getMark(r.getRate()).getValue(),
                r.getRateDate().toGregorianCalendar().getTimeInMillis());
    }

    public IResult<Rating> getRatingsByMessageId(int messageId) throws StorageException {
        return helper.execute(Converters.TO_RATING, DataQuery.GET_OBJECTS_RATING_BY_MESSAGE_ID, messageId);
    }

    public IResult<MarkStat> getMarkStatByMessageId(int messageId) throws StorageException {
        return helper.execute(
                Converters.TO_MARK_STAT,
                DataQuery.GET_OBJECTS_MARK_STAT_BY_MESSAGE_ID,
                messageId
        );
    }
}
