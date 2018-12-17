package com.xgx.musicplay;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.view.MenuItem;

import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lucasurbas.listitemview.ListItemView;

import java.util.List;

/**
 * Created by xgx on 2018/12/13 for MusicPlayDemo
 */
public class MusicHistoryListAdapter extends BaseQuickAdapter<Music, BaseViewHolder> {
    public MusicHistoryListAdapter(@Nullable List<Music> data) {
        super(R.layout.adapter_music_history, data);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void convert(final BaseViewHolder helper, final Music item) {
        ListItemView listItemView = helper.getView(R.id.list_item_view);
        listItemView.setTitle(item.getName());
        listItemView.setSubtitle(item.getMusicDir() + "\n扫描时间：" + item.getScantime() + "\n播放时间：" + StringUtils.null2Length0(item.getPlaytime()));
        if (item.isPrize()) {
            listItemView.getAvatarView().setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.zhongjiang));

        } else {
            listItemView.getAvatarView().setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.nozhongjiang));

        }
        listItemView.setOnMenuItemClickListener(new ListItemView.OnMenuItemClickListener() {

            @Override
            public void onActionMenuItemSelected(final MenuItem menu) {
                // click
                if (menu.getItemId() == R.id.action_remove) {
                    remove(helper.getLayoutPosition());
                    MyApplication.getDaoInstant().getMusicDao().delete(item);
                    ((HistoryListActivity)mContext).changeNum();
                }
            }
        });
    }
}
