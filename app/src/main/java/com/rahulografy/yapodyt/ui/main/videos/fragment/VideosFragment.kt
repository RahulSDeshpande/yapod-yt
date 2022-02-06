package com.rahulografy.yapodyt.ui.main.videos.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.rahulografy.yapodyt.BR
import com.rahulografy.yapodyt.R
import com.rahulografy.yapodyt.data.model.videos.VideoItem
import com.rahulografy.yapodyt.databinding.FragmentVideosBinding
import com.rahulografy.yapodyt.ui.base.view.BaseFragment
import com.rahulografy.yapodyt.ui.main.activity.MainActivityViewModel
import com.rahulografy.yapodyt.ui.main.searchfilter.SearchFiltersFragment
import com.rahulografy.yapodyt.ui.main.videoplayer.VideoPlayerFragment
import com.rahulografy.yapodyt.ui.main.videos.adapter.VideosAdapter
import com.rahulografy.yapodyt.ui.main.videos.listener.VideoListListener
import com.rahulografy.yapodyt.util.ext.isNotNullOrEmpty
import com.rahulografy.yapodyt.util.ext.list
import com.rahulografy.yapodyt.util.ext.show
import com.rahulografy.yapodyt.util.ext.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideosFragment :
    BaseFragment<FragmentVideosBinding, VideosFragmentViewModel>(),
    VideoListListener {

    private lateinit var videosAdapter: VideosAdapter

    private var searchFiltersFragment: SearchFiltersFragment? = null

    override val layoutRes get() = R.layout.fragment_videos

    override val toolbarId: Int get() = R.id.toolbar_videos

    override val bindingVariable = BR.viewModel

    override val vm: VideosFragmentViewModel by viewModels()

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_videos, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (vm.isDataLoading.get()) {
            toast(getString(R.string.msg_fetching_data_please_wait))
        } else {
            when (item.itemId) {
                R.id.menu_action_filter -> openSearchFiltersFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun initUi() {
        getVideos()
        initSwipeRefreshLayout()
    }

    override fun initObservers() {

        mainActivityViewModel
            .videoCategoryItems
            .observe(
                lifecycleOwner = this,
                observer = { videoCategoryItems ->
                    if (videoCategoryItems.isNotNullOrEmpty()) {
                        updateCategoryHeader()
                        vm.getVideos(
                            force = true,
                            videoCategoryId = getVideoCategoryId()
                        )
                    } else {
                        toast(getString(R.string.msg_no_video_categories))
                    }
                }
            )

        mainActivityViewModel
            .videoCategoryItemUpdated
            .observe(
                lifecycleOwner = this,
                observer = {
                    updateCategoryHeader()
                    vm.getVideos(
                        force = true,
                        videoCategoryId = getVideoCategoryId()
                    )
                }
            )

        vm.videoItems
            .observe(
                lifecycleOwner = this,
                observer = { videoItems ->
                    initVideosRecyclerView(videoItems = videoItems)
                }
            )
    }

    private fun updateCategoryHeader() {
        vdb.textViewVideosCategory.text =
            mainActivityViewModel.videoCategoryItem?.snippet?.title
    }

    private fun getVideos() {

        if (mainActivityViewModel.videoCategoryItems.value.isNullOrEmpty() ||
            mainActivityViewModel.videoCategoryItem == null
        ) {
            vm.isDataLoading.set(true)
            mainActivityViewModel.getVideoCategories()
        } else {
            vm.getVideos(
                force = true,
                videoCategoryId = getVideoCategoryId()
            )
        }
    }

    private fun getVideoCategoryId() = mainActivityViewModel.videoCategoryItem?.id

    private fun initSwipeRefreshLayout() {
        vdb.swipeRefreshLayoutVideos.setOnRefreshListener {
            getVideos()
        }
    }

    private fun initVideosRecyclerView(videoItems: List<VideoItem>?) {

        if (videoItems.isNullOrEmpty().not()) {
            videosAdapter = VideosAdapter(videoListListener = this)
            vdb.recyclerViewVideos.adapter = videosAdapter
            vdb.recyclerViewVideos.list()

            videosAdapter.submitList(videoItems)

            showVideosRecyclerView(show = true)
        } else {
            showVideosRecyclerView(show = false)
        }
    }

    private fun showVideosRecyclerView(show: Boolean) {
        vdb.recyclerViewVideos.show(show = show)
        vdb.layoutNoData.show(show = show.not())
    }

    override fun onVideoClicked(
        listPosition: Int,
        videoItem: VideoItem
    ) {
        openVideoPlayerFragment(
            listPosition = listPosition,
            videoItem = videoItem
        )
    }

    private fun openSearchFiltersFragment() {
        if (searchFiltersFragment == null) {
            searchFiltersFragment = SearchFiltersFragment()
        }
        searchFiltersFragment?.show(childFragmentManager, searchFiltersFragment?.tag)
    }

    private fun openVideoPlayerFragment(
        listPosition: Int,
        videoItem: VideoItem
    ) {
        mainActivityViewModel.selectedVideoItem = videoItem

        val videoPlayerFragment = VideoPlayerFragment()
        videoPlayerFragment.show(childFragmentManager, videoPlayerFragment.tag)
    }
}
