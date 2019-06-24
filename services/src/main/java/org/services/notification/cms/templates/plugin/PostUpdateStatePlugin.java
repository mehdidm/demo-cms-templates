package org.services.notification.cms.templates.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.State;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.services.notification.cms.templates.utils.NotificationConstants;
import org.services.notification.cms.templates.utils.NotificationUtils;

public class PostUpdateStatePlugin extends BaseNotificationPlugin {
  private static final Log                    LOG                   = ExoLogger.getLogger(PostUpdateStatePlugin.class);

  public final static String                  ID                    = "PostUpdateStatePlugin";

  OrganizationService                         orgService            = WCMCoreUtils.getService(OrganizationService.class);

  UserHandler                                 userhandler           = orgService.getUserHandler();

  MembershipHandler                           membershipHandler     = orgService.getMembershipHandler();

  public final static ArgumentLiteral<String> CONTENT_TITLE         = new ArgumentLiteral<String>(String.class, "CONTENT_TITLE");

  public final static ArgumentLiteral<String> CONTENT_UPDATER       =
                                                              new ArgumentLiteral<String>(String.class, "CONTENT_UPDATER");

  public final static ArgumentLiteral<String> CONTENT_STATUS        = new ArgumentLiteral<String>(String.class, "CONTENT_STATUS");

  public final static ArgumentLiteral<String> CONTENT_URL           = new ArgumentLiteral<String>(String.class, "CONTENT_URL");

  public final static ArgumentLiteral<String> PUBLICATION_LIFECYCLE = new ArgumentLiteral<String>(String.class,
                                                                                                  "PUBLICATION_LIFECYCLE");

  IdentityManager                             identityManager;

  public PostUpdateStatePlugin(InitParams initParams, IdentityManager identityManager) {

    super(initParams);
    this.identityManager = identityManager;

  }

  public PostUpdateStatePlugin(InitParams initParams) {

    super(initParams);

  }

  @Override

  public String getId() {

    return ID;

  }

  @Override

  public boolean isValid(NotificationContext ctx) {

    return true;

  }

  @Override

  protected NotificationInfo makeNotification(NotificationContext ctx) {

    String contentTitle = ctx.value(CONTENT_TITLE);
    String contentStatus = ctx.value(CONTENT_STATUS);
    String contentUpdater = ctx.value(CONTENT_UPDATER);
    String contentUrl = ctx.value(CONTENT_URL);
    String lifecycle = ctx.value(PUBLICATION_LIFECYCLE);
    List<String> receiversIds = new ArrayList<String>();
    try {
      List<User> receivers = getReceivers(contentStatus, lifecycle);
      receivers.forEach(u -> receiversIds.add(u.getUserName()));
    } catch (Exception e) {
      LOG.error("An error occured when trying to have the list of receivers "+e.getMessage());
    }
    // remove redondance
    Set<String> receiversSet = new HashSet<String>(receiversIds);

    if (contentStatus.equals(PublicationDefaultStates.DRAFT)) {
      receiversSet.remove(contentUpdater);
    }
    // convert the set to List to be used after in to method
    List<String> receiverIdsList = new ArrayList(receiversSet);
    return NotificationInfo.instance()

                           .setFrom(contentUpdater)
                           .with(NotificationConstants.CONTENT_TITLE, contentTitle)
                           .to(receiverIdsList)
                           .with(NotificationConstants.CONTENT_STATUS, contentStatus)
                           .with(NotificationConstants.CONTENT_UPDATER, contentUpdater)
                           .with(NotificationConstants.CONTENT_URL, contentUrl)
                           .key(getKey())
                           .end();

  }

  private List<User> getReceivers(String contentStatus, String lifecycleName) throws Exception {

    List<User> receivers = new ArrayList<User>();
    List<State> stateList = NotificationUtils.getStates(lifecycleName);
    if (contentStatus.equals(PublicationDefaultStates.DRAFT)) {
      for (State state : stateList) {
        if (state.getState().equals(contentStatus) || state.getState().equals(PublicationDefaultStates.PENDING)) {

          receivers = this.getAllUsersByState(state, receivers);
        }

      }
    }

    if (contentStatus.equals(PublicationDefaultStates.PENDING)) {
      for (State state : stateList) {
        if (state.getState().equals(contentStatus) || state.getState().equals(PublicationDefaultStates.APPROVED)) {

          receivers = this.getAllUsersByState(state, receivers);
        }

      }
    }
    if (contentStatus.equals(PublicationDefaultStates.APPROVED)) {
      for (State state : stateList) {
        if (state.getState().equals(contentStatus) || state.getState().equals(PublicationDefaultStates.PENDING)
            || state.getState().equals(PublicationDefaultStates.STAGED)
            || state.getState().equals(PublicationDefaultStates.PUBLISHED)) {

          receivers = this.getAllUsersByState(state, receivers);
        }

      }
    }
    if (contentStatus.equals(PublicationDefaultStates.STAGED)) {
      for (State state : stateList) {
        if (state.getState().equals(contentStatus) || state.getState().equals(PublicationDefaultStates.PENDING)
            || state.getState().equals(PublicationDefaultStates.APPROVED)
            || state.getState().equals(PublicationDefaultStates.PUBLISHED)) {

          receivers = this.getAllUsersByState(state, receivers);
        }

      }
    }
    if (contentStatus.equals(PublicationDefaultStates.PUBLISHED)) {
      for (State state : stateList) {
        if (state.getState().equals(contentStatus) || state.getState().equals(PublicationDefaultStates.PENDING)
            || state.getState().equals(PublicationDefaultStates.APPROVED)
            || state.getState().equals(PublicationDefaultStates.STAGED)) {

          receivers = this.getAllUsersByState(state, receivers);
        }

      }
    }
    return receivers;

  }

  private List<User> getAllUsersByState(State state, List<User> receivers) throws Exception {

    String membership = state.getMembership();
    if (membership != null) {

      receivers.addAll(getAllUserByMemberShip(membership, receivers));

    }
    List<String> memberships = state.getMemberships();
    if (memberships != null) {
      for (String m : memberships) {
        receivers.addAll(getAllUserByMemberShip(m, receivers));
      }
    }
    return receivers;
  }

  private List<User> getAllUserByMemberShip(String membership, List<User> receivers) throws Exception {
    String[] membershipTab = membership.split(":");
    String membershipType = membershipTab[0];
    String group = membershipTab[1];
    ListAccess<User> userList = userhandler.findUsersByGroupId(group);
    for (User user : Arrays.asList(userList.load(0, userList.getSize()))) {
      Collection<Membership> membershipsCollection = membershipHandler.findMembershipsByUserAndGroup(user.getUserName(), group);
      for (Membership ms : membershipsCollection) {
        if (membershipType.equals(ms.getMembershipType()) || ms.getMembershipType().equals("*")) {
          receivers.add(user);
        }
      }

    }
    return receivers;
  }
}
