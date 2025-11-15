package com.example.domain.useCase.staff

import com.example.domain.repo.StaffRepo

class RejectOrRehireUseCase(private val staffRepo: StaffRepo) {
    suspend operator fun invoke(employeeId: String, reject: Boolean): Pair<Boolean, String> {
       return staffRepo.fireOrRehireEmployee(employeeId, reject)
    }
}