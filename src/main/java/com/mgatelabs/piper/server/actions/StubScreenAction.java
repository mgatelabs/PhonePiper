package com.mgatelabs.piper.server.actions;

import com.google.common.collect.Lists;
import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.ui.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/24/2018
 */
public class StubScreenAction implements EditActionInterface {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String execute(final String id, final String value, final EditHolder holder) {

        if (StringUtils.isNotBlank(id) && Constants.ID_PATTERN.matcher(id).matches()) {

            for (ScreenDefinition screenDefinition : holder.getViewDefinition().getScreens()) {
                if (screenDefinition.getScreenId().equals(id)) {
                    logger.info("Screen with same ID already exists");
                    return "Screen with same ID already exists";
                }
            }

            ScreenDefinition screenDefinition = new ScreenDefinition();
            screenDefinition.setScreenId(id);
            screenDefinition.setName(id);

            screenDefinition.setPoints(Lists.newArrayList());
            screenDefinition.setEnabled(false);

            holder.getViewDefinition().getScreens().add(screenDefinition);
            holder.getViewDefinition().sort();
            holder.getViewDefinition().save();

            return "";
        } else {
            return "Screen ID can only contain (a-z A-Z 0-9 - _)";
        }
    }
}
