package org.services.notification.cms.templates.utils;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.wcm.extensions.publication.PublicationManager;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.Lifecycle;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.State;

public class NotificationUtils {
  private static final Log LOG = ExoLogger.getLogger(NotificationUtils.class.getName());

  public static List<State> getStates(String lifecycleName) throws Exception {
    List<State> states = new ArrayList<State>();
    PublicationManager publicationManagerImpl = CommonsUtils.getService(PublicationManager.class);
    Lifecycle lifecycle = publicationManagerImpl.getLifecycle(lifecycleName);
    states = lifecycle.getStates();
    return states;
  }

}
