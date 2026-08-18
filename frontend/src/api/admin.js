import api from './index'

export const users = (page, size, role, deleted) => api.get('/admin/users', { params: { page, size, role, deleted } })
export const setUserStatus = (id, status) => api.put(`/admin/user/${id}/status`, { status })
export const stores = (page, size, auditStatus) => api.get('/admin/stores', { params: { page, size, auditStatus } })
export const auditStore = (id, data) => {
  // 支持两种调用方式：auditStore(id, auditStatus) 或 auditStore(id, { auditStatus, rejectReason })
  const body = typeof data === 'object' ? data : { auditStatus: data }
  return api.put(`/admin/store/${id}/audit`, body)
}
