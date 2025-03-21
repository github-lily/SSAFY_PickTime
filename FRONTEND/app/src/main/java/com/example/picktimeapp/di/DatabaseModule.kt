package com.example.picktimeapp.di

import android.content.Context
import androidx.room.Room
import com.example.picktimeapp.data.db.AppDatabase
import com.example.picktimeapp.data.db.dao.LessonDao
import com.example.picktimeapp.data.db.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "picktime_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideLessonDao(appDatabase: AppDatabase): LessonDao {
        return appDatabase.lessonDao()
    }
}