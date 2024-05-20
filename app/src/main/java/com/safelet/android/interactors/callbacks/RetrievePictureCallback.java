/**
 * Created by Catalin Clabescu on Sep 19, 2014
 * Copyright 2014 XL Team. All rights reserved
 */
package com.safelet.android.interactors.callbacks;

import android.graphics.Bitmap;

/**
 * Callback interface for profile picture retrieval
 *
 * @author catalin
 */
public interface RetrievePictureCallback {
    /**
     * Called when picture is successfully retrieved
     *
     * @param bitmap User picture
     */
    void onRetrieveSuccess(Bitmap bitmap);

    /**
     * Called when an error occurs
     */
    void onRetrieveFailed();
}
