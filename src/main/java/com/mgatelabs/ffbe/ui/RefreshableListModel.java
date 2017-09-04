package com.mgatelabs.ffbe.ui;

import javax.swing.*;
import java.util.List;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/3/2017.
 */
public class RefreshableListModel<E> extends AbstractListModel<E> {
    private List<E> data;

    public RefreshableListModel(List<E> data) {
        this.data = data;
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public E getElementAt(int index) {
        return data.get(index);
    }

    public void refresh() {
        fireContentsChanged(data, 0,0);
    }
}
