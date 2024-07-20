new Vue({
    el: '#app',
    data: {
        seats: [
            { id: 1, seats: [
                    { id: 101, label: '1樓: 座位1', status: 'available' },
                    { id: 102, label: '1樓: 座位2', status: 'available' },
                    { id: 103, label: '1樓: 座位3', status: 'available' },
                    { id: 104, label: '1樓: 座位4', status: 'available' }
                ]},
            { id: 2, seats: [
                    { id: 201, label: '2樓: 座位1', status: 'available' },
                    { id: 202, label: '2樓: 座位2', status: 'available' },
                    { id: 203, label: '2樓: 座位3', status: 'available' },
                    { id: 204, label: '2樓: 座位4', status: 'available' }
                ]},
            { id: 3, seats: [
                    { id: 301, label: '3樓: 座位1', status: 'available' },
                    { id: 302, label: '3樓: 座位2', status: 'available' },
                    { id: 303, label: '3樓: 座位3', status: 'available' },
                    { id: 304, label: '3樓: 座位4', status: 'available' }
                ]},
            { id: 4, seats: [
                    { id: 401, label: '4樓: 座位1', status: 'available' },
                    { id: 402, label: '4樓: 庸位2', status: 'available' },
                    { id: 403, label: '4樓: 座位3', status: 'available' },
                    { id: 404, label: '4樓: 座位4', status: 'available' }
                ]}
        ],
        employeeIds: [],
        selectedEmployeeId: null,
        previouslySelectedSeat: null
    },
    methods: {
        toggleSeat(selectedSeat) {
            if (selectedSeat.status === 'select') {
                selectedSeat.status = selectedSeat.originalStatus;
                this.previouslySelectedSeat = null;
            } else {
                if (this.previouslySelectedSeat) {
                    if (this.previouslySelectedSeat.status === 'select') {
                        this.previouslySelectedSeat.status = this.previouslySelectedSeat.originalStatus;
                    }
                }
                if (selectedSeat.status === 'full' || selectedSeat.status === 'available') {
                    selectedSeat.originalStatus = selectedSeat.status;
                    selectedSeat.status = 'select';
                }
                this.previouslySelectedSeat = selectedSeat;
            }
        },
        findSeatById(seatId) {
            for (const row of this.seats) {
                for (const seat of row.seats) {
                    if (seat.id === seatId) {
                        return seat;
                    }
                }
            }
            return null;
        },
        loadEmployeeIds() {
            fetch('http://localhost:7070/getId')
                .then(response => response.json())
                .then(data => {
                    this.employeeIds = data;
                    if (this.employeeIds.length > 0) {
                        this.selectedEmployeeId = this.employeeIds[0];
                    }
                })
                .catch(error => {
                    console.error('Error fetching employee IDs:', error);
                });
        },
        loadSeatStatus() {
            fetch('http://localhost:7070/getSeat')
                .then(response => response.json())
                .then(data => {
                    data.forEach(seatInfo => {
                        const seat = this.findSeatByFloorAndNumber(seatInfo.FLOOR_SEAT_SEQ);
                        if (seat) {
                            seat.status = 'full';
                            seat.label += ` [員編:${seatInfo.EMP_ID}]`;
                        }
                    });
                })
                .catch(error => {
                    console.error('Error fetching seat status:', error);
                });
        },
        findSeatByFloorAndNumber(floorSeatSeq) {
            for (const row of this.seats) {
                for (const seat of row.seats) {
                    if (seat.id === floorSeatSeq) {
                        return seat;
                    }
                }
            }
            return null;
        },
        submitSelection() {
            if (!this.previouslySelectedSeat || !this.selectedEmployeeId) {
                alert('請先選擇員工ID與座位');
                return;
            }

            const payload = {
                empId: this.selectedEmployeeId,
                floorSeatSeq: this.previouslySelectedSeat.id
            };

            fetch('http://localhost:7070/updateSeat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            })
                .then(response => response.text())
                .then(data => {
                    alert(data);
                    location.reload();
                })
                .catch(error => {
                    console.error('Error updating seat status:', error);
                });
        }
    },
    created() {
        this.loadEmployeeIds();
        this.loadSeatStatus();
    }
});