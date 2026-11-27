package com.watashi.schemas.LinkedList;

/**
 * Definition for a singly-linked list.
 *
 * <p>
 * Based on the {@code ListNode} implementation provided by
 * <a href="https://leetcode.com/">LeetCode</a>.
 * </p>
 *
 * @author watashi00
 * @see <a href="https://leetcode.com/">LeetCode</a>
 */

public class ListNode {
    int value;
    ListNode next;

    ListNode() {
    }

    ListNode(int value) {
        this.value = value;
    }

    ListNode(int value, ListNode next) {
        this.value = value;
        this.next = next;
    }

}
