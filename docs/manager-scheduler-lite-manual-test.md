# Manager Scheduler Lite manual test checklist

- [ ] Log in with a manager or administrator account and confirm direct arrival at Dashboard.
- [ ] Confirm an assistant or HR account cannot log in to Scheduler Lite.
- [ ] Add a doctor with a default office, workdays, color, and notes.
- [ ] Add multiple assistants and assign their eligible/default offices.
- [ ] Create a reusable team containing the doctor and assistants.
- [ ] Duplicate the team and confirm its membership was copied relationally.
- [ ] Create or open a monthly schedule.
- [ ] Assign the team to multiple dates and modify one date without changing the reusable template.
- [ ] Save the schedule, leave the page, reopen it, and confirm assignments persist.
- [ ] Open Print Schedule and use the print button.
- [ ] In Letter landscape preview, confirm the month title and right border fit and no controls/navigation print.
- [ ] Confirm manager APIs return 401 without a JWT and 403 for unsupported roles.
- [ ] Confirm inactive resources cannot be added to a reusable team.
