import { TaskItem, TaskList } from '@tiptap/extension-list';

/**
 * 체크박스 형태의 할 일 목록.
 * `- [ ]` 입력으로 생성되며, 슬래시 메뉴의 `/todo`로도 삽입할 수 있다.
 * 스타일은 globals.css의 `ul[data-type='taskList']`에서 정의된다.
 */
export const TaskListNode = TaskList;

export const TaskItemNode = TaskItem.configure({
  nested: true,
});
