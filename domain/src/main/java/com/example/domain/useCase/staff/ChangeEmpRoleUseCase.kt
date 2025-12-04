package com.example.domain.useCase.staff

import com.example.domain.repo.StaffRepo

class ChangeEmpRoleUseCase(private val repo: StaffRepo) {
    suspend operator fun invoke(newRole: Int, empId: String): Pair<Boolean, String> {
        return repo.changeRoleOfEmployee(newRole, empId)
    }
}