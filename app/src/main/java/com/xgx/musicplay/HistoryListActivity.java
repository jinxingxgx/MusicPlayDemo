package com.xgx.musicplay;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;

/**
 * Created by xgx on 2018/12/13 for MusicPlayDemo
 */
public class HistoryListActivity extends Activity {
    @BindView(R.id.titlebar)
    CommonTitleBar titlebar;
    @BindView(R.id.lv)
    RecyclerView lv;
    private List<Music> data;
    private MusicHistoryListAdapter mAdapter;
    private long delayMillis = 500;
    private long TOTAL_COUNTER;
    private int mCurrentCounter;
    private boolean isErr = true;
    private int page = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        titlebar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == titlebar.ACTION_LEFT_TEXT || action == titlebar.ACTION_LEFT_BUTTON) {
                    finish();
                }
            }

        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // 设置布局管理器
        lv.setLayoutManager(layoutManager);
        mAdapter = new MusicHistoryListAdapter(data);
        mAdapter.openLoadAnimation();

        mAdapter.setEnableLoadMore(true);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                lv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentCounter >= TOTAL_COUNTER) {
                            //数据全部加载完毕
                            mAdapter.loadMoreEnd();
                        } else {
                            if (isErr) {
                                //成功获取更多数据
                                getData();

                                mCurrentCounter = mAdapter.getData().size();
                                mAdapter.loadMoreComplete();
                            } else {
                                //获取更多数据失败
                                isErr = true;
                                mAdapter.loadMoreFail();

                            }
                        }
                    }

                }, delayMillis);
            }
        }, lv);
        lv.setAdapter(mAdapter);
        page = 0;
        getData();
    }

    private void getData() {
        //根据page分页

        MusicDao dao = MyApplication.getDaoInstant().getMusicDao();
        QueryBuilder builder = dao.queryBuilder();
        TOTAL_COUNTER = builder.count();
        List<Music> listMsg = builder.orderDesc(MusicDao.Properties.Id).offset(page * 10).limit(10).list();
        mAdapter.addData(listMsg);
        page++;
    }
}
