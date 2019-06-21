package org.services.notification.cms.templates.provider;

import java.io.Writer;
import java.util.Locale;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.webui.utils.TimeConvertUtils;
import org.gatein.common.text.EntityEncoder;
import org.services.notification.cms.templates.plugin.PostUpdateStatePlugin;
import org.services.notification.cms.templates.utils.NotificationUtils;

import com.ibm.icu.util.Calendar;
@TemplateConfigs(
	templates = {
		@TemplateConfig( pluginId = PostUpdateStatePlugin.ID, template="war:/notification/templates/web/postUpdateStatePlugin.gtmpl")
		   }
		)
public class WebTemplateProvider extends TemplateProvider  {
	  protected static Log log = ExoLogger.getLogger(WebTemplateProvider.class);

	  public WebTemplateProvider(InitParams initParams) {
	    super(initParams);
	    this.templateBuilders.put(PluginKey.key(PostUpdateStatePlugin.ID), new TemplateBuilder());
	  }

	  private class TemplateBuilder extends AbstractTemplateBuilder {
	    @Override
	    protected MessageInfo makeMessage(NotificationContext ctx) {
	      NotificationInfo notification = ctx.getNotificationInfo();
	      String pluginId = notification.getKey().getId();      

	      String language = getLanguage(notification);
	      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), pluginId, language);

	      String contentUpdater = notification.getValueOwnerParameter(NotificationUtils.CONTENT_UPDATER);
	      String contentTitle = notification.getValueOwnerParameter(NotificationUtils.CONTENT_TITLE);
	      String contentStatus = notification.getValueOwnerParameter(NotificationUtils.CONTENT_STATUS);

	      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
	      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
	      if( contentUpdater == null || IdentityConstants.SYSTEM.equals(contentUpdater)) {
		      // System
		      templateContext.put("USER", "SYSTEM");
		      templateContext.put("AVATAR", "/eXoSkin/skin/images/system/UserAvtDefault.png");
		      templateContext.put("PROFILE_URL", "#");
	      } else {
		      Identity author = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, contentUpdater, true);
		      Profile profile = author.getProfile();
		      //creator
		      templateContext.put("USER", encoder.encode(profile.getFullName()));
		      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(profile));
		      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", author.getRemoteId()));
	      }
	      templateContext.put("CONTENT_TITLE", encoder.encode(contentTitle));
	      templateContext.put("CONTENT_STATUS", encoder.encode(contentStatus));
	      templateContext.put("CONTENT_UPDATER", encoder.encode(contentUpdater));


	      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read" : "unread");
	      templateContext.put("NOTIFICATION_ID", notification.getId());
	      Calendar lastModified = Calendar.getInstance();
	      lastModified.setTimeInMillis(notification.getLastModifiedDate());
	      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgoByTimeServer(lastModified.getTime(),"EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));

	      //
	      String body = TemplateUtils.processGroovy(templateContext);
	      //binding the exception throws by processing template
	      ctx.setException(templateContext.getException());
	      MessageInfo messageInfo = new MessageInfo();
	      return messageInfo.body(body).end();
	    }

	    @Override
	    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
	      return false;
	    }

	  };

}
