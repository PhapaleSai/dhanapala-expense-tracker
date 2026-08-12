package com.phapalesai.dhanapala.ui.screens.splitgroups

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.SplitExpenseEntity
import com.phapalesai.dhanapala.data.local.SplitGroupEntity
import com.phapalesai.dhanapala.domain.DebtSettlement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupDetail(
    val group: SplitGroupEntity,
    val expenses: List<SplitExpenseEntity>,
    val balances: Map<String, Double>,
    val settlements: List<DebtSettlement.Payment>
)

@OptIn(ExperimentalCoroutinesApi::class)
class SplitGroupsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val repo = app.splitRepository

    val groups: StateFlow<List<SplitGroupEntity>> = repo.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    val selectedGroupId: StateFlow<Long?> = _selectedGroupId

    val selectedGroupDetail: StateFlow<GroupDetail?> = _selectedGroupId.flatMapLatest { id ->
        if (id == null) {
            flowOf(null)
        } else {
            combine(groups, repo.observeExpenses(id)) { allGroups, expenses ->
                val group = allGroups.firstOrNull { it.id == id } ?: return@combine null
                val participants = group.participants.split(",").filter { it.isNotBlank() }
                val balances = computeBalances(participants, expenses)
                GroupDetail(group, expenses, balances, DebtSettlement.settle(balances))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectGroup(id: Long?) {
        _selectedGroupId.value = id
    }

    fun createGroup(name: String, participants: List<String>) {
        if (name.isBlank() || participants.size < 2) return
        viewModelScope.launch { repo.createGroup(name.trim(), participants.map { it.trim() }) }
    }

    fun addExpense(groupId: Long, description: String, amount: Double, paidBy: String, splitAmong: List<String>) {
        if (description.isBlank() || amount <= 0 || paidBy.isBlank() || splitAmong.isEmpty()) return
        viewModelScope.launch { repo.addExpense(groupId, description.trim(), amount, paidBy, splitAmong) }
    }

    fun deleteGroup(groupId: Long) {
        if (_selectedGroupId.value == groupId) _selectedGroupId.value = null
        viewModelScope.launch { repo.deleteGroup(groupId) }
    }

    private fun computeBalances(participants: List<String>, expenses: List<SplitExpenseEntity>): Map<String, Double> {
        val balances = participants.associateWith { 0.0 }.toMutableMap()
        for (expense in expenses) {
            val among = expense.splitAmong.split(",").filter { it.isNotBlank() }.ifEmpty { participants }
            val share = expense.amount / among.size
            balances[expense.paidBy] = (balances[expense.paidBy] ?: 0.0) + expense.amount
            among.forEach { person -> balances[person] = (balances[person] ?: 0.0) - share }
        }
        return balances
    }
}
