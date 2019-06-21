package org.services.listener;
import javax.jcr.Node;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.services.notification.cms.templates.plugin.PostUpdateStatePlugin;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationConstant;


public class PostUpdateStateListener extends Listener<Object,Node>   {
	private static final Log LOG = ExoLogger.getLogger(PostUpdateStateListener.class.getName());

	  @Override
	  public void onEvent(Event<Object,Node> event) throws Exception {
	      LOG.info("the status was updated");
	      Node content = event.getData();
	      String contentTitle = content.getProperty(NodetypeConstant.EXO_TITLE).getValue().getString();
	      String contentUpdater = content.getProperty(NodetypeConstant.EXO_LAST_MODIFIER).getValue().getString();
	      String contentStatus = content.getProperty(AuthoringPublicationConstant.CURRENT_STATE).getValue().getString();

	   // Send Notification
	      NotificationContext ctx = NotificationContextImpl.cloneInstance().append(PostUpdateStatePlugin.CONTENT_TITLE, contentTitle)
	    		  .append(PostUpdateStatePlugin.CONTENT_STATUS, contentStatus)
	    		  .append(PostUpdateStatePlugin.CONTENT_UPDATER, contentUpdater);
	      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(PostUpdateStatePlugin.ID))).execute(ctx);
	      LOG.info("notification was sent");
	}
}
