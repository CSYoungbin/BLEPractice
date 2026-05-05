package com.youngdevsbin.whoopblepractice.di

import android.content.Context
import com.youngdevsbin.whoopblepractice.ble.BleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BleModule {
    @Provides
    @Singleton
    fun provideBleRepository(@ApplicationContext context: Context): BleRepository {
        return BleRepository(context)
    }
}
