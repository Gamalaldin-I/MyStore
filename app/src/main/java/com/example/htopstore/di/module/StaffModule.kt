package com.example.htopstore.di.module

import com.example.data.remote.repo.StaffRepoImp
import com.example.domain.repo.StaffRepo
import com.example.domain.useCase.staff.AcceptInviteUseCase
import com.example.domain.useCase.staff.AddStoreInviteUseCase
import com.example.domain.useCase.staff.DeleteStoreInviteUseCase
import com.example.domain.useCase.staff.GetAllEmailPendingInvitesUseCase
import com.example.domain.useCase.staff.GetStoreEmployeesUseCase
import com.example.domain.useCase.staff.GetStoreInvitesUseCase
import com.example.domain.useCase.staff.RejectInviteUseCase
import com.example.domain.useCase.staff.RejectOrRehireUseCase
import com.example.domain.useCase.staff.UpdateStoreDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StaffModule {
    @Singleton
    @Provides
    fun provideStaffRepo(): StaffRepo {
        return StaffRepoImp()
    }
    @Provides
    fun provideInvitesUseCase(staffRepo: StaffRepo): GetStoreInvitesUseCase {
        return GetStoreInvitesUseCase(staffRepo)}
    @Provides
    fun provideEmployeesUseCase(staffRepo: StaffRepo): GetStoreEmployeesUseCase{
        return GetStoreEmployeesUseCase(staffRepo)
    }
    @Provides
    fun provideDeleteInviteUseCase(staffRepo: StaffRepo): DeleteStoreInviteUseCase{
        return DeleteStoreInviteUseCase(staffRepo)
    }
    @Provides
    fun provideAddInviteUseCase(staffRepo: StaffRepo): AddStoreInviteUseCase{
        return AddStoreInviteUseCase(staffRepo)
    }
    @Provides
    fun provideAllEmailPendingInvitesUseCase(staffRepo: StaffRepo): GetAllEmailPendingInvitesUseCase{
        return GetAllEmailPendingInvitesUseCase(staffRepo)
    }
    @Provides
    fun provideAcceptInviteUseCase(staffRepo: StaffRepo): AcceptInviteUseCase{
        return AcceptInviteUseCase(staffRepo)
    }
    @Provides
    fun provideRejectInviteUseCase(staffRepo: StaffRepo): RejectInviteUseCase{
        return RejectInviteUseCase(staffRepo)
    }
    @Provides
    fun provideRejectOrRehireUseCase(staffRepo: StaffRepo): RejectOrRehireUseCase{
        return RejectOrRehireUseCase(staffRepo)
    }
    @Provides
    fun provideUpdateStoreUseCase(staffRepo: StaffRepo): UpdateStoreDataUseCase{
        return UpdateStoreDataUseCase(staffRepo)
    }


}