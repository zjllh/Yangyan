package com.xxp.yangyan.pro.ui.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.xxp.yangyan.R;
import com.xxp.yangyan.pro.adapter.ImageAdapter;
import com.xxp.yangyan.pro.api.AnalysisHTML;
import com.xxp.yangyan.pro.base.BaseSwipeFragment;
import com.xxp.yangyan.pro.bean.ImageInfo;
import com.xxp.yangyan.pro.listener.BaseOnScrollListener;
import com.xxp.yangyan.pro.mvp.contract.ImgListContract;
import com.xxp.yangyan.pro.mvp.presenter.ImgListPresenterImpl;
import com.xxp.yangyan.pro.ui.activity.GalleryActivity;
import com.xxp.yangyan.pro.utils.UIUtils;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.ResponseBody;

import static com.xxp.yangyan.R.id.swipe;

/**
 * Created by 钟大爷 on 2017/2/3.
 */

public class ImageCityFragment extends BaseSwipeFragment<ImgListPresenterImpl>
        implements SwipeRefreshLayout.OnRefreshListener, ImgListContract.view {
    private final String TAG = "ImageCityFragment";
    @BindView(R.id.tagFlowLayout)
    TagFlowLayout tagFlowLayout;
    @BindView(R.id.rv_images)
    RecyclerView rvImages;
    //谷歌的刷新空间
    @BindView(swipe)
    SwipeRefreshLayout swipeRefresh;
    private BaseOnScrollListener listener;
    private RecyclerView.LayoutManager layoutManager;
    //存放图片
    private List<ImageInfo> imageInfos;
    private ImageAdapter imageAdapter;
    private ProgressDialog mDialog;
    private int currentPosition;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFlowLayout();
        initRecyclerView();
    }

    @Override
    protected SwipeRefreshLayout getSwipeRefresh() {
        return swipeRefresh;
    }


    private void initRecyclerView() {
        imageInfos = new ArrayList<>();
        listener = new BaseOnScrollListener<ImageInfo>() {
            @Override
            protected List<ImageInfo> getList() {
                return imageInfos;
            }

            @Override
            protected RecyclerView getRecylerView() {
                return rvImages;
            }

            @Override
            public RecyclerView.LayoutManager getLayoutManager() {
                return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            }

            @Override
            public SwipeRefreshLayout getRefreshLayout() {
                return swipeRefresh;
            }

            @Override
            protected RecyclerView.Adapter getAdapter() {
                if (imageAdapter == null) {
                    imageAdapter = new ImageAdapter(imageInfos, ImageCityFragment.this, null);
                }
                return imageAdapter;
            }


            @Override
            protected void loadData() {
                    presenter.getData(null, getCurrentPage());
            }
        };
        imageAdapter.setOnItemClick(new ImageAdapter.OnItemClick() {
            @Override
            public void onClickListener(int position) {
                currentPosition = position;
                mDialog = new ProgressDialog(getActivity());
                presenter.loadGallery(imageInfos.get(position),mDialog);
            }
        });

    }


    private void initFlowLayout() {
        final List<String> types = new ArrayList<>();
        types.add("童颜巨乳");
        types.add("泳池派对");
        types.add("美");
        types.add("动感丝袜");
        types.add("学生装");
        types.add("制服诱惑");
        types.add("动感丝袜");
        tagFlowLayout.setAdapter(new TagAdapter<String>(types) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                View view = UIUtils.getInflaterView(R.layout.item_flowlayout);
                TextView type = (TextView) view.findViewById(R.id.tv_type);
                type.setText(s);
                return view;
            }
        });
        tagFlowLayout.setVisibility(View.GONE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_imagecity;
    }

    @Override
    protected ImgListPresenterImpl onCreatePresenter() {
        return new ImgListPresenterImpl(this);
    }

    @Override
    public void onRefresh() {
        listener.refresh();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public void showResult(ResponseBody data) {
        try {
            List<ImageInfo> imageInfos = AnalysisHTML.HomePageToList(data.string());
            int listCount = imageInfos.size();
            if (listCount == 0) {
                listener.setEnd(true);
            } else {
                this.imageInfos.addAll(imageInfos);
                int pos = imageInfos.size();
                if (pos > 0) {
                    pos--;
                    imageAdapter.notifyItemChanged(pos, listCount);
                    swipeRefresh.setRefreshing(false);
                    listener.setLoading(false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void loadGalleryError() {
        mDialog.setMessage("阿欧!加载失败");
    }

    @Override
    public void loadGallerySuccess(ResponseBody responseBody) {
        List<ImageInfo> images = new ArrayList<>();
        try {
            images = (AnalysisHTML.ParticularsToList(responseBody.string(),
                    imageInfos.get(currentPosition).getLink()));
            mDialog.dismiss();
            Intent intent = new Intent(UIUtils.getContext(), GalleryActivity.class);
            intent.putExtra("gallery", (Serializable) images);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadLocalDataError() {

    }

    @Override
    public void loadLocalDataSuccess(List<ImageInfo> imageInfos) {

    }

    @Override
    public void loadLocalDataIsEmpty() {

    }

    @Override
    public void showSuccess() {

    }

    @Override
    public void showError(Throwable throwable) {
        loadError();
        Log.e(TAG, "showError: " + throwable);
    }


}