package org.xblackcat.rojac.service.storage.database;

import org.xblackcat.utils.ResourceUtils;

/**
 * @author ASUS
 */

public enum DataQuery implements IPropertiable {
    // All object getting queries
    /**
     * The query should fetch the version object properties in following order:
     * <p/>
     * type(int), version_data(byte[64])
     */
    GET_OBJECTS_VERSION,
    /**
     * The query should fetch the forum group object properties in following order:
     * <p/>
     * id(int), name(String), sort_order(int)
     */
    GET_OBJECTS_FORUM_GROUP,
    /**
     * The query should fetch the forum object properties in following order:
     * <p/>
     * id(int), forum group(int), rated(int), inTop(int), rateLimit(int), subscribed(boolean), short name(String),
     * name(String)
     */
    GET_OBJECTS_FORUM,
    /**
     * The query should fetch the new rating object properties in following order:
     * <p/>
     * id(int), message id(int), rate(int)
     */
    GET_OBJECTS_NEW_RATING,
    /**
     * The query should fetch the message data properties in following order:
     * <p/>
     * id (int), topic id (int), parent id (int), forum id (int), user id (int), subject (String), user nick (String),
     * message date (long), update date (long), read (boolean), rating (String)
     *
     * Parameter: threadId (int)
     */
    GET_OBJECTS_MESSAGE_DATA,
    /**
     * The query should fetch the moderate object properties in following order:
     * <p/>
     * message id(int), user id(int), forum id(int), creation time(long)
     */
    GET_OBJECTS_MODERATE,
    /**
     * The query should fetch the new message object properties in following order:
     * <p/>
     * id (int), parent id(int), forum id(int), subject(String), message(String)
     */
    GET_OBJECTS_NEW_MESSAGE,
    /**
     * The query should fetch the new moderate object properties in following order:
     * <p/>
     * id (int), message id (int), action (int), forum id(int), description(String), asModerator(boolean)
     */
    GET_OBJECTS_NEW_MODERATES,
    /**
     * The query should fetch the user object properties in following order:
     * <p/>
     * id (int), name (String), nick (String), real_name (String), email (String), home_page (String), specialization
     * (String), where_from (String), origin (String)
     */
    GET_OBJECTS_USER,
    /**
     * The query should fetch the rating object properties in following order:
     * <p/>
     * message id(int), topic id(int), user id(int), user rating(int), rate(int), rate date(long)
     */
    GET_OBJECTS_RATING,

    // All insert object queries
    /**
     * The query for storing the version object. Set the object parameters in following order:
     * <p/>
     * type(int), version_data(byte[64])
     */
    STORE_OBJECT_VERSION,
    /**
     * The query for storing the forum group object. Set the object parameters in following order:
     * <p/>
     * id(int), name(String), sort_order(int)
     */
    STORE_OBJECT_FORUM_GROUP,
    /**
     * The query for storing the forum object. Set the object parameters in following order:
     * <p/>
     * id(int), forum group(int), rated(int), inTop(int), rateLimit(int), subscribed(boolean), short name(String),
     * name(String)
     */
    STORE_OBJECT_FORUM,
    /**
     * The query for storing the new rating object. Set the object parameters in following order:
     * <p/>
     * id(int), message id(int), rate(int)
     */
    STORE_OBJECT_NEW_RATING,
    /**
     * The query for storing the new moderate object. Set the object parameters in following order:
     * <p/>
     * id (int), message id (int), action (int), forum id(int), description(String), asModerator(boolean)
     */
    STORE_OBJECT_NEW_MODERATE,
    /**
     * The query for storing the message object. Set the object parameters in following order:
     * <p/>
     * id (int), topic id (int), parent id (int), user id (int), forum id (int), article id (int), user title color
     * (int), user role (int), notify on response (boolean), read (boolean), favorite (int), message date (long), update
     * date (long), moderated date (long), subject (String), message name (String), user nick (String), user title
     * (String), message (String), recent_child_date(int)
     */
    STORE_OBJECT_MESSAGE,
    /**
     * The query for storing the moderate object. Set the object parameters in following order:
     * <p/>
     * message id(int), user id(int), forum id(int), creation time(long)
     */
    STORE_OBJECT_MODERATE,
    /**
     * The query for storing the new message object. Set the object parameters in following order:
     * <p/>
     * parent id(int), forum id(int), subject(String), message(String)
     */
    STORE_OBJECT_NEW_MESSAGE,
    /**
     * The query for storing the user object. Set the object parameters in following order:
     * <p/>
     * id (int), name (String), nick (String), real_name (String), email (String), home_page (String), specialization
     * (String), where_from (String), origin (String)
     */
    STORE_OBJECT_USER,
    /**
     * The query for storing the rating object. Set the object parameters in following order:
     * <p/>
     * message id(int), topic id(int), user id(int), user rating(int), rate(int), rate date(long)
     */
    STORE_OBJECT_RATING,
    /**
     * The query for storing the extra messages to download. Set the parameter:
     * <p/>
     * messageId (int)
     */
    STORE_OBJECT_EXTRA_MESSAGE,

    // Get objects by its properties
    /**
     * The query should fetch the rating object properties in following order:
     * <p/>
     * message id(int), topic id(int), user id(int), user rating(int), rate(int), rate date(long)
     */
    GET_OBJECTS_RATING_BY_MESSAGE_ID,
    /**
     * The query should fetch the moderate object properties in following order:
     * <p/>
     * message id(int), user id(int), forum id(int), creation time(long)
     */
    GET_OBJECTS_MODERATE_BY_MESSAGE_ID,
    /**
     * The query should fetch the new rating object properties in following order:
     * <p/>
     * id(int), message id(int), rate(int)
     */
    GET_OBJECTS_NEW_RATING_BY_MESSAGE_ID,

    // Object removing queries.
    /**
     * The query for removing the forum object by its id
     */
    REMOVE_OBJECT_FORUM,
    /**
     * The query fir removing the forum group object by its id
     */
    REMOVE_OBJECT_FORUM_GROUP,
    /**
     * The query for removing the user object by its id
     */
    REMOVE_OBJECT_USER,
    /**
     * The query for removing the new rating object by its id
     */
    REMOVE_OBJECT_NEW_RATING,
    /**
     * The query for removing the new message object by its id
     */
    REMOVE_OBJECT_NEW_MESSAGE,
    /**
     * The query for removing the new moderate object by its id
     */
    REMOVE_OBJECT_NEW_MODERATE,
    /**
     * The query for removing the message object by its id
     */
    REMOVE_OBJECT_MESSAGE,
    /**
     * The query for removing the extra message from the table
     */
    REMOVE_OBJECT_EXTRA_MESSAGE,
    /**
     * The query for removing the rating objects by message id.
     */
    REMOVE_OBJECTS_RATING,
    /**
     * The query for removing the moderate objects by message id.
     */
    REMOVE_OBJECTS_MODERATE,
    /**
     * The query for clearing all the extra messages.
     */
    REMOVE_ALL_OBJECTS_EXTRA_MESSAGE,
    REMOVE_ALL_OBJECTS_NEW_RATING,

    // Queries for getting all the ids of the objects
    GET_IDS_FORUM,
    GET_IDS_FORUM_GROUP,
    GET_IDS_NEW_RATING,
    GET_IDS_NEW_MESSAGE,
    GET_IDS_MESSAGE,
    GET_IDS_USER,
    GET_IDS_EXTRA_MESSAGE,

    // Queries for getting object by its id
    GET_OBJECT_VERSION,
    GET_OBJECT_FORUM,
    GET_OBJECT_FORUM_GROUP,
    GET_OBJECT_MESSAGE,
    GET_OBJECT_USER,
    /**
     * The query should fetch the new message object properties in following order:
     * <p/>
     * id(int), message id(int), rate(int)
     */
    GET_OBJECT_NEW_MESSAGE,
    /**
     * The query should fetch the new moderate object properties in following order:
     * <p/>
     * id (int), message id (int), action (int), forum id(int), description(String), asModerator(boolean)
     */
    GET_OBJECT_NEW_MODERATE,
    /**
     * The query should fetch the message data properties in following order:
     * <p/>
     * id (int), topic id (int), parent id (int), forum id (int), user id (int), subject (String), user nick (String),
     * message date (long), update date (long), read (boolean)
     *
     * Parameter: messageId (int)
     */
    GET_OBJECT_MESSAGE_DATA,

    // Queries for getting parts of object by object id
    GET_OBJECT_MESSAGE_BODY,
    /**
     * Returns int values - weight of mark
     */
    GET_OBJECTS_RATING_MARK_BY_MESSAGE_ID,
    GET_OBJECTS_NEW_RATING_MARK_BY_MESSAGE_ID,
    GET_OBJECTS_MARK_STAT_BY_MESSAGE_ID,

    // Queries for getting next id value for object primary key
    GET_NEXT_ID_NEW_RATING,
    GET_NEXT_ID_NEW_MESSAGE,

    // Other queries
    /**
     * Query for updating the version info object. SQL paramerets are:
     * <p/>
     * version_data(byte[64]), version_type (int)
     */
    UPDATE_OBJECT_VERSION,
    /**
     * The query for updating the message object. Set the object parameters in following order:
     * <p/>
     * topic id (int), parent id (int), user id (int), forum id (int), article id (int), user title color (int), user
     * role (int), notify on response (boolean), read (boolean), favorite (int), message date (long), update date
     * (long), moderated date (long), subject (String), message name (String), user nick (String), user title (String),
     * message (String), id (int)
     */
    UPDATE_OBJECT_MESSAGE,
    /**
     * The query for updating the forum group object. Set the object parameters in following order:
     * <p/>
     * name(String), sort_order(int), id(int)
     */
    UPDATE_OBJECT_FORUM_GROUP,
    /**
     * The query for updating the forum object (except <code>subscribe </code> field). Set the object parameters in
     * following order:
     * <p/>
     * forum group(int), rated(int), inTop(int), rateLimit(int), short name(String), name(String), id(int)
     */
    UPDATE_OBJECT_FORUM,
    /**
     * The query for updating the subscribe field of forum object. Set the object parameters in following order:
     * <p/>
     * subscribed(boolean), id(int)
     */
    UPDATE_OBJECT_FORUM_SUBSCRIBE,
    /**
     * The query for updating the last_child_date filed of message object. Set query parameters in following order:
     * <p/>
     * last_child_date(long), id(int)
     */
    UPDATE_MESSAGE_RECENT_CHILD_DATE,
    /**
     * The query for updating the read field of message object. Set query parameters in following order:
     * <p/>
     * read(boolean), id(int)
     */
    UPDATE_MESSAGE_READ_FLAG,
    /**
     * The query for updating the read field of message objects: thread root and all the belongs posts. Set query parameters in following order:
     * <p/>
     * read(boolean), id(int)
     */
    UPDATE_THREAD_READ_FLAG,
    /**
     * The query for updating the read field of messages in the specified forum. Set query parameters in following
     * order:
     * <p/>
     * read(boolean), id(int)
     */
    UPDATE_FORUM_MESSAGES_READ_FLAG,
    /**
     * The query for updating a new message object. Set the object parameters in following order:
     * <p/>
     * parent id(int), forum id(int), subject(String), message(String), id (int)
     */
    UPDATE_OBJECT_NEW_MESSAGE,
    /**
     * Query for updating ratings cache for message.
     *
     * rating cache (string), message id (int)
     */
    UPDATE_MESSAGE_RATING_CACHE,

    GET_TOPIC_MESSAGE_DATA_BY_FORUM_ID,
    /**
     * The query for retrieving ids for the specified forum group. SQL parameters:
     * <p/>
     * forum_group_id(int)
     */
    GET_IDS_FORUM_BY_FORUM_GROUP,
    GET_IDS_FORUM_SUBSCRIBED,
    GET_IDS_MESSAGE_BY_PARENT_ID,
    GET_IDS_MESSAGE_BY_TOPIC_ID,
    /**
     * Returns messages number in forum. Parameter is forumId.
     */
    GET_MESSAGES_NUMBER_IN_FORUM,
    /**
     * Returns unread messages number in forum. Parameter is forumId.
     */
    GET_UNREAD_MESSAGES_NUMBER_IN_FORUM,
    /**
     * Returns unread messages number.
     */
    GET_UNREAD_MESSAGES_NUMBER,
    /**
     * Returns unread replies number. Parameter is targetUserId.
     */
    GET_UNREAD_REPLIES_NUMBER,
    /**
     * Returns unread messages number in thread. Parameter is threadId.
     */
    GET_UNREAD_MESSAGES_NUMBER_IN_THREAD,
    /**
     * Returns date of last message in forum and amount of replays. Parameter is threadId.
     */
    GET_THREAD_STAT_DATA,
    /**
     * Returns last message date in forum. Parameter is forumId.
     */
    GET_LAST_MESSAGE_DATE_IN_FORUM,
    IS_FORUM_HAS_MESSAGES,
    /**
     * Checks is the message id exist
     */
    IS_MESSAGES_EXIST,
    GET_IDS_TOPIC_MESSAGE_BY_FORUM_ID,
    /**
     * Returns two properties for message object: parent id and last_child_date. The properties is returned in following
     * order:
     * <p/>
     * parent_id(int)
     */
    GET_PARENT_ID_FOR_MESSAGE_ID,
    GET_BROKEN_TOPIC_IDS;

    private final String propertyName = ResourceUtils.constantToProperty(this.name());

    public String getPropertyName() {
        return propertyName;
    }

    public String toString() {
        return super.toString() + '[' + propertyName + ']';
    }
}
