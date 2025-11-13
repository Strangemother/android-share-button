package me.talofa.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Bottom sheet for selecting a group
 */
class GroupSelectionBottomSheet : BottomSheetDialogFragment() {
    
    private var groups: List<Group> = emptyList()
    private var onGroupSelected: ((Group) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_group_selection, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val recyclerView = view.findViewById<RecyclerView>(R.id.groupsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = GroupAdapter(groups) { group ->
            onGroupSelected?.invoke(group)
            dismiss()
        }
    }
    
    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }
    
    fun setGroups(groups: List<Group>): GroupSelectionBottomSheet {
        this.groups = groups
        return this
    }
    
    fun setOnGroupSelectedListener(listener: (Group) -> Unit): GroupSelectionBottomSheet {
        this.onGroupSelected = listener
        return this
    }
    
    fun setOnDismissListener(listener: () -> Unit): GroupSelectionBottomSheet {
        this.onDismissListener = listener
        return this
    }
    
    private class GroupAdapter(
        private val groups: List<Group>,
        private val onItemClick: (Group) -> Unit
    ) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group, parent, false)
            return GroupViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
            holder.bind(groups[position], onItemClick)
        }
        
        override fun getItemCount(): Int = groups.size
        
        class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameTextView: TextView = itemView.findViewById(R.id.groupNameTextView)
            private val descriptionTextView: TextView = itemView.findViewById(R.id.groupDescriptionTextView)
            
            fun bind(group: Group, onItemClick: (Group) -> Unit) {
                nameTextView.text = group.name
                
                if (!group.description.isNullOrEmpty()) {
                    descriptionTextView.text = group.description
                    descriptionTextView.visibility = View.VISIBLE
                } else {
                    descriptionTextView.visibility = View.GONE
                }
                
                itemView.setOnClickListener {
                    onItemClick(group)
                }
            }
        }
    }
}
