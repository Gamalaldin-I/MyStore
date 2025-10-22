package com.example.domain.useCase.staff

import com.example.domain.repo.StaffRepo

class GetStoreEmployeesUseCase(private val staffRepo: StaffRepo){
     operator fun invoke() {
        return staffRepo.listenToEmployees()
    }
}