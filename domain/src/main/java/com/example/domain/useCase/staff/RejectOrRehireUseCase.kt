package com.example.domain.useCase.staff

import com.example.domain.repo.StaffRepo

class RejectOrRehireUseCase(private val staffRepo: StaffRepo) {
    operator fun invoke(employeeId: String, reject: Boolean, onResult: (Boolean, String) -> Unit) {
        staffRepo.rejectOrRehireEmployee(employeeId, reject, onResult)
    }
}