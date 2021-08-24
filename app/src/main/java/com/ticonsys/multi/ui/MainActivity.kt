package com.ticonsys.multi.ui

import com.ticonsys.base.BaseActivity
import com.ticonsys.multi.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun initializeViewBinding() = ActivityMainBinding.inflate(layoutInflater)

}