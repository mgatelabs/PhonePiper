package com.mgatelabs.piper.server.actions;

import com.google.common.collect.Lists;
import com.mgatelabs.piper.server.EditHolder;
import com.mgatelabs.piper.shared.details.ScreenDefinition;
import com.mgatelabs.piper.ui.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by @mgatelabs (Michael Fuller) on 4/1/2020 for Phone-Piper.
 */
public class RenameScreenAction implements EditActionInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String execute(final String id, final String value, final EditHolder holder) {

        if (StringUtils.isNotBlank(id) && Constants.ID_PATTERN.matcher(id).matches()) {

            if (StringUtils.isBlank(value)) {
                return "New screen name is required";
            }

            ScreenDefinition screenDefinition = holder.getScreenForId(id);
            if (screenDefinition == null) return "Could not find screen with id: " + id;

            screenDefinition.setName(value);

            holder.getViewDefinition().sort();
            holder.getViewDefinition().save();

            return "";
        } else {
            return "Screen ID can only contain (a-z A-Z 0-9 - _)";
        }
    }
}
