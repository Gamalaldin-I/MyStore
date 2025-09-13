package com.example.htopstore.di

import android.content.Context
import com.example.htopstore.data.local.repo.archieve.ArchiveRepoImp
import com.example.htopstore.domain.useCase.archive.DeleteProductUseCase
import com.example.htopstore.domain.useCase.archive.GetArchiveProductsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object ArchiveModule {
    @Provides
    fun provideArchiveViewModel(@ApplicationContext context: Context): ArchiveRepoImp {
        return ArchiveRepoImp(context)
    }
    @Provides
    fun provideGetArchiveProductsUseCase(archiveRepoImp: ArchiveRepoImp): GetArchiveProductsUseCase {
        return GetArchiveProductsUseCase(archiveRepoImp)
    }
    @Provides
    fun provideDeleteProductUseCase(archiveRepoImp: ArchiveRepoImp): DeleteProductUseCase {
        return DeleteProductUseCase(archiveRepoImp)
    }
}