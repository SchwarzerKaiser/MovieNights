package com.leewilson.moviebee.ui.main.feed

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.leewilson.moviebee.R
import com.leewilson.moviebee.databinding.FragmentFeedBinding
import com.leewilson.moviebee.model.MovieNight
import com.leewilson.moviebee.ui.main.BaseMainFragment
import com.leewilson.moviebee.util.Constants
import com.leewilson.moviebee.util.TopSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : BaseMainFragment(R.layout.fragment_feed) {

    private companion object {
        private const val TAG = "FeedFragment"
    }

    private lateinit var binding: FragmentFeedBinding
    private val viewModel: FeedViewModel by viewModels()
    private lateinit var adapter: FeedAdapter

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(layoutInflater)
        return binding.root
    }

    /*
    1) View movienights from friends, in a specific order (TBD)
    2) Like movienights - add to likes list for movienight entity
    3) Comment on movienights - add to comment list for movienight entity
    4) Nav to movienight detail screen
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val uid = sharedPrefs.getString(Constants.CURRENT_USER_UID, "")!!

        adapter = FeedAdapter(

            object : DiffUtil.ItemCallback<MovieNight>() {
                override fun areItemsTheSame(oldItem: MovieNight, newItem: MovieNight): Boolean {
                    return oldItem == newItem
                }
                override fun areContentsTheSame(oldItem: MovieNight, newItem: MovieNight): Boolean {
                    return oldItem == newItem
                }
            },

            object : FeedAdapter.Interaction {
                override fun onLike(movieNight: MovieNight) {
                    viewModel.like(movieNight)
                }

                override fun onUnLike(movieNight: MovieNight) {
                    viewModel.unlike(movieNight)
                }
            },

            uid
        )

        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }

        feedRecyclerView.adapter = adapter
        feedRecyclerView.addItemDecoration(TopSpacingItemDecoration(16))
        feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        collectPagingData()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search_users, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuitem_add_user -> {
                findNavController().navigate(R.id.action_feedFragment_to_userSearchFragment)
                return true
            }
        }
        return false
    }

    private fun collectPagingData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.flow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}