package com.example.domain.useCase.staff

import com.example.domain.model.User
import com.example.domain.repo.StaffRepo

class GetStoreEmployeesUseCase(private val staffRepo: StaffRepo){
     suspend operator fun invoke(): Pair<List<User>,String> {
        return staffRepo.getEmployees()
    }
}