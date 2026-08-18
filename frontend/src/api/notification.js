import api from '@/api'

export function getNotificationList(params) {
  return api.get('/notification/list', { params })
}

export function getUnreadCount() {
  return api.get('/notification/unread-count')
}

export function markAsRead(id) {
  return api.post(`/notification/${id}/read`)
}

export function markAllAsRead() {
  return api.post('/notification/read-all')
}

export function deleteNotification(id) {
  return api.delete(`/notification/${id}`)
}

export function getRecentNotifications(limit = 5) {
  return api.get('/notification/recent', { params: { limit } })
}
