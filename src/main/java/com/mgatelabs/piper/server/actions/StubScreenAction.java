package com.mgatelabs.piper.server.actions;

import com.google.common.collect.Lists;
import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.ui.utils.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.logging.Logger;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/24/2018
 */
public class StubScreenAction implements EditActionInterface {
    @Override
    public String execute(final String id, final String value, final EditHolder holder, Logger logger) {

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
