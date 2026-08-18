import api from './index'

export const getRiderInfo = () => api.get('/rider/info')
export const toggleOnline = (online) => api.post('/rider/online', { online })
export const getGrabList = () => api.get('/rider/grab/list')
export const grabOrder = (orderId) => api.post(`/rider/grab/${orderId}`)
export const getDeliveryList = (status) => api.get('/rider/delivery/list', { params: { status } })
export const pickup = (id) => api.post(`/rider/delivery/${id}/pickup`)
export const deliver = (id) => api.post(`/rider/delivery/${id}/deliver`)
export const getEarningsStats = () => api.get('/rider/earnings/stats')
export const getEarnings = (params = {}) => api.get('/rider/earnings', { params })
export const heartbeat = () => api.post('/rider/heartbeat')
