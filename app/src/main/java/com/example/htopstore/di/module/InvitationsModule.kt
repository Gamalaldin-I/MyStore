package com.example.htopstore.di.module

import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.repo.InvitationsRepoImp
import com.example.domain.repo.InvitationsRepo
import com.example.domain.useCase.invitations.AcceptInviteUseCase
import com.example.domain.useCase.invitations.AddStoreInviteUseCase
import com.example.domain.useCase.invitations.DeleteStoreInviteUseCase
import com.example.domain.useCase.invitations.GetAllEmailPendingInvitesUseCase
import com.example.domain.useCase.invitations.GetStoreInvitesUseCase
import com.example.domain.useCase.invitations.RejectInviteUseCase
import com.example.domain.useCase.invitations.SendInvitationMailUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InvitationsModule {
    @Provides
    @Singleton
    fun provideInvitationRepoImp(
        supabaseClient: SupabaseClient,
        pref: SharedPref
    ): InvitationsRepo{
        return InvitationsRepoImp(
            supabase = supabaseClient,
            pref = pref
        )
    }



    @Provides
    fun provideDeleteInviteUseCase(repo: InvitationsRepo): DeleteStoreInviteUseCase{
        return DeleteStoreInviteUseCase(repo)
    }
    @Provides
    fun provideAddInviteUseCase(repo: InvitationsRepo): AddStoreInviteUseCase{
        return AddStoreInviteUseCase(repo)
    }
    @Provides
    fun provideAllEmailPendingInvitesUseCase(repo: InvitationsRepo): GetAllEmailPendingInvitesUseCase{
        return GetAllEmailPendingInvitesUseCase(repo)
    }
    @Provides
    fun provideAcceptInviteUseCase(repo: InvitationsRepo): AcceptInviteUseCase{
        return AcceptInviteUseCase(repo)
    }
    @Provides
    fun provideRejectInviteUseCase(repo: InvitationsRepo): RejectInviteUseCase{
        return RejectInviteUseCase(repo)
    }

    @Provides
    fun provideInvitesUseCase(repo: InvitationsRepo): GetStoreInvitesUseCase {
        return GetStoreInvitesUseCase(repo)
    }
    @Provides
    fun provideSendEmailUseCase(
        repo: InvitationsRepo): SendInvitationMailUseCase{
        return SendInvitationMailUseCase(repo)
    }
}