package limitless.android.minimalmusic.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import limitless.android.minimalmusic.db.MainRepository
import limitless.android.minimalmusic.db.local.MusicRepository
import limitless.android.minimalmusic.db.local.MusicRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SongModule {

    @Provides
    @Singleton
    fun provideMusicRepository() : MusicRepository = MusicRepositoryImpl()

    @Provides
    @Singleton
    fun provideMainRepository(musicRepository: MusicRepository) : MainRepository = MainRepository(musicRepository)

}