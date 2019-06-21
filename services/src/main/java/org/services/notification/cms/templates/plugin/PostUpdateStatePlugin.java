package org.services.notification.cms.templates.plugin;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.manager.IdentityManager;
import org.services.notification.cms.templates.utils.NotificationUtils;


public class PostUpdateStatePlugin extends BaseNotificationPlugin {
	private static final Log LOG = ExoLogger.getLogger(PostUpdateStatePlugin.class);

	public final static String ID = "PostUpdateStatePlugin";

	public final static ArgumentLiteral<String> CONTENT_TITLE = new ArgumentLiteral<String>(String.class,"CONTENT_TITLE");
	public final static ArgumentLiteral<String> CONTENT_UPDATER = new ArgumentLiteral<String>(String.class,"CONTENT_UPDATER");
	public final static ArgumentLiteral<String> CONTENT_STATUS = new ArgumentLiteral<String>(String.class,"CONTENT_STATUS");

	IdentityManager identityManager;

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
		
		Set<String> receivers = new HashSet<String>();

		return NotificationInfo.instance()

				.setFrom(contentUpdater).with(NotificationUtils.CONTENT_TITLE, contentTitle)
				.with(NotificationUtils.CONTENT_STATUS, contentStatus)
				.with(NotificationUtils.CONTENT_UPDATER, contentUpdater)
				.setSendAll(true).key(getKey()).end();

	}
}
