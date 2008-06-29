package org.xblackcat.sunaj.service.janus;

import org.apache.axis.Chain;
import org.apache.axis.Handler;
import org.apache.axis.SimpleChain;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.CommonsHTTPSender;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xblackcat.sunaj.data.NewMessage;
import org.xblackcat.sunaj.data.NewRating;
import org.xblackcat.sunaj.data.Version;
import org.xblackcat.sunaj.service.ServiceFactory;
import org.xblackcat.sunaj.service.janus.data.ForumsList;
import org.xblackcat.sunaj.service.janus.data.NewData;
import org.xblackcat.sunaj.service.janus.data.PostInfo;
import org.xblackcat.sunaj.service.janus.data.TopicMessages;
import org.xblackcat.sunaj.service.janus.data.UsersList;
import org.xblackcat.sunaj.service.options.IOptionsService;
import org.xblackcat.sunaj.service.options.Property;
import ru.rsdn.Janus.*;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;

/**
 * Date: 10 ��� 2007
 *
 * @author Alexey
 */

public class JanusService implements IJanusService {
    private static final Log log = LogFactory.getLog(JanusService.class);

    private final String userName;
    private final String password;

    private JanusATSoap soap;

    public static IJanusService getInstance(String userName, String password) throws JanusServiceException {
        try {
            JanusService c = new JanusService(userName, password);
            c.init();
            c.testConnection();
            return c;
        } catch (ServiceException e) {
            throw new JanusServiceException(e);
        }
    }

    private JanusService(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public void testConnection() throws JanusServiceException {
        log.debug("Perform check connection.");

        try {
            soap.check();
        } catch (RemoteException e) {
            throw new JanusServiceException(e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Check have done successfuly.");
        }
    }

    public ForumsList getForumsList() throws JanusServiceException {
        log.info("Retrieve the forums list from the Janus WS.");

        ForumResponse list;
        try {
            list = soap.getForumList(new ForumRequest(userName, password));
        } catch (RemoteException e) {
            throw new JanusServiceException("Can not obtain the forum list.", e);
        }

        JanusForumInfo[] forumInfos = list.getForumList();
        JanusForumGroupInfo[] groupInfos = list.getGroupList();
        if (log.isDebugEnabled()) {
            log.debug("Got " + forumInfos.length + " forum(s) in " + groupInfos.length + " group(s).");
        }
        return new ForumsList(forumInfos, groupInfos);
    }

    public UsersList getNewUsers(Version verRow, int maxOutput) throws JanusServiceException {
        log.info("Retrieve the users list from the Janus WS. Limit = " + maxOutput);

        UserResponse list;
        try {
            list = soap.getNewUsers(new UserRequest(userName, password, verRow.getBytes(), maxOutput));
        } catch (RemoteException e) {
            throw new JanusServiceException("Can not obtain the new users list.", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Got " + list.getUsers().length + " new user(s)");
        }
        return new UsersList(list);
    }

    public TopicMessages getTopicByMessage(int[] messageIds) throws JanusServiceException {
        log.info("Retrieve the topics by messages from the Janus WS.");
        if (log.isDebugEnabled()) {
            log.debug("Messages ids are: " + ArrayUtils.toString(messageIds));
        }

        TopicResponse list;
        try {
            list = soap.getTopicByMessage(new TopicRequest(userName, password, messageIds));
        } catch (RemoteException e) {
            throw new JanusServiceException("Can not obtain the new users list.", e);
        }
        JanusMessageInfo[] messages = list.getMessages();
        JanusModerateInfo[] moderate = list.getModerate();
        JanusRatingInfo[] rating = list.getRating();
        if (log.isDebugEnabled()) {
            log.debug("Got " + messages.length + " message(s), " + moderate.length + " modarators note(s) and " + rating.length + " user raiting(s).");
        }
        return new TopicMessages(messages, moderate, rating);
    }

    public PostInfo commitChanges() throws JanusServiceException {
        log.info("Commit the local changes to the Janus WS.");

        PostResponse post;
        try {
            post = soap.postChangeCommit();
        } catch (RemoteException e) {
            throw new JanusServiceException("Can not obtain the new users list.", e);
        }
        PostExceptionInfo[] exceptions = post.getExceptions();
        int[] ids = post.getCommitedIds();
        if (log.isDebugEnabled()) {
            log.debug(ids.length + " message(s) commited and " + exceptions.length + " exception(s) occurs.");
        }
        return new PostInfo(ids, exceptions);
    }

    public void postChanges(NewMessage[] messages, NewRating[] ratings) throws JanusServiceException {
        log.info("Post the changes to the Janus WS.");

        PostMessageInfo[] newMessages = new PostMessageInfo[messages.length];
        for (int i = 0; i < messages.length; i++) {
            NewMessage m = messages[i];
            newMessages[i] = new PostMessageInfo(m.getLocalMessageId(),
                    m.getParentId(),
                    m.getForumId(),
                    m.getSubject(),
                    m.getMessage());
        }

        PostRatingInfo[] newRates = new PostRatingInfo[ratings.length];
        for (int i = 0; i < ratings.length; i++) {
            NewRating r = ratings[i];
            newRates[i] = new PostRatingInfo(r.getMessageId(), r.getRate().getValue());
        }

        try {
            soap.postChange(new PostRequest(userName, password, newMessages, newRates));
        } catch (RemoteException e) {
            throw new JanusServiceException("Can not obtain the new users list.", e);
        }
    }

    public NewData getNewData(int[] subscribedForums, boolean[] firstForumRequest, Version ratingVer, Version messageVer, Version moderateVer, int[] breakMsgIds, int[] breakTopicIds, int maxOutput) throws JanusServiceException {
        log.info("Retrieve the messages from the Janus WS.");

        boolean hasIndividualConfig = firstForumRequest != null && subscribedForums.length == firstForumRequest.length;

        RequestForumInfo[] rfi = new RequestForumInfo[subscribedForums.length];
        for (int i = 0; i < subscribedForums.length; i++) {
            rfi[i] = new RequestForumInfo(subscribedForums[i], hasIndividualConfig && firstForumRequest[i]);
        }

        ChangeResponse list;
        try {
            list = soap.getNewData(new ChangeRequest(userName, password,
                    rfi,
                    ratingVer.getBytes(),
                    messageVer.getBytes(),
                    moderateVer.getBytes(),
                    breakMsgIds, breakTopicIds,
                    maxOutput));
        } catch (RemoteException e) {
            throw new JanusServiceException("Can not obtain the new users list.", e);
        }
        Version forumRowVersion = new Version(list.getLastForumRowVersion());
        Version ratingRowVersion = new Version(list.getLastRatingRowVersion());
        Version moderateRowVerion = new Version(list.getLastModerateRowVersion());

        JanusMessageInfo[] newMessages = list.getNewMessages();
        JanusModerateInfo[] newModerate = list.getNewModerate();
        JanusRatingInfo[] newRating = list.getNewRating();

        int ownId = list.getUserId();

        if (log.isDebugEnabled()) {
            log.debug("New data statistics: " +
                    "own user id is " + ownId + ", " +
                    newMessages.length + " new message(s), " +
                    newRating.length + " new rating(s), " +
                    newModerate.length + " new moderate info(s)" +
                    ", forums version is " + forumRowVersion +
                    ", rating version is " + ratingRowVersion +
                    ", moderate version is " + moderateRowVerion
            );
        }
        return new NewData(ownId, forumRowVersion, ratingRowVersion, ratingRowVersion, newMessages, newModerate, newRating);
    }

    private void init() throws ServiceException {
        IOptionsService os = ServiceFactory.getInstance().getOptionsService();

        log.info("Janus SAOP service initialization has been started.");

        JanusATLocator jl = new JanusATLocator();
        SimpleProvider clientConfig = new SimpleProvider();
        Chain reqHandler = new SimpleChain();
        Chain respHandler = new SimpleChain();

        Handler pivot = new CommonsHTTPSender();
        Handler transport = new SimpleTargetedChain(reqHandler, pivot, respHandler);
        clientConfig.deployTransport(HTTPTransport.DEFAULT_TRANSPORT_NAME, transport);
        jl.setEngineConfiguration(clientConfig);
        jl.setEngine(new AxisClient(clientConfig));
        jl.setMaintainSession(true);

        JanusATSoapStub soap = (JanusATSoapStub) jl.getJanusATSoap();

        // Compress the request
//		soap._setProperty(HTTPConstants.MC_GZIP_REQUEST, Boolean.TRUE);
        if (os.getProperty(Property.SERVICE_JANUS_USE_GZIP)) {
            log.info("Data compression is enabled.");
            // Tell the server it can compress the response
            soap._setProperty(HTTPConstants.MC_ACCEPT_GZIP, Boolean.TRUE);
        }

        this.soap = soap;
        log.info("Initialization has done.");
    }
}