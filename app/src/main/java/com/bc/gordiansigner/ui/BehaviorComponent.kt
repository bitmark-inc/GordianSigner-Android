package com.bc.gordiansigner.ui

interface BehaviorComponent {

    /**
     * Refresh stuff like view, data or something
     */
    fun refresh() {}

    fun onBackPressed(): Boolean = false
}