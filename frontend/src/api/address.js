import api from './index'

export const addressList = () => api.get('/address/list')
export const saveAddress = (data) => api.post('/address/save', data)
export const deleteAddress = (id) => api.delete(`/address/${id}`)
