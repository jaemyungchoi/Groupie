import React from 'react';
import { List, Label, Item } from 'semantic-ui-react'
import moment from 'moment';

const ProposalListItem = (props) => {
    const { proposal } = props;
    const url = `/proposal?id=${proposal.id}`;
    const finalized = proposal.finalizedEvent !== null;
    return (
        <Item>
            <Item.Content>
                <Item.Header as='a' href={url} content={proposal.title} />
                <Item.Meta>
                    Created by {proposal.owner.username} on {moment.utc(proposal.createdAt).format('MMM Do YYYY, h:mm a')}
                </Item.Meta>
                <Item.Description>
                    <List horizontal>
                        <List.Item>
                            <List.Content>
                                <List.Header content='Invited Users' />
                                <List.Description>{proposal.invitedUsers.map(user => user.username).join(', ')}</List.Description>
                            </List.Content>
                        </List.Item>
                        {
                            finalized && proposal.acceptedUsers.length > 0 &&
                            <List.Item>
                                <List.Content>
                                    <List.Header content='Accepted Users' />
                                    <List.Description>{proposal.acceptedUsers.map(user => user.username).join(', ')}</List.Description>
                                </List.Content>
                            </List.Item>
                        }
                        {
                            finalized && proposal.declinedUsers.length > 0 &&
                            <List.Item>
                                <List.Content>
                                    <List.Header content='Declined Users' />
                                    <List.Description>{proposal.declinedUsers.map(user => user.username).join(', ')}</List.Description>
                                </List.Content>
                            </List.Item>
                        }
                        {
                            finalized && proposal.pendingUsers.length > 0 &&
                            <List.Item>
                                <List.Content>
                                    <List.Header content='Undecided Users' />
                                    <List.Description>{proposal.pendingUsers.map(user => user.username).join(', ')}</List.Description>
                                </List.Content>
                            </List.Item>
                        }
                        {
                            finalized && proposal.tmEventData &&
                            <List.Item>
                                <List.Content>
                                    <List.Header content='Best Event' />
                                    <List.Description>
                                        {proposal.tmEventData.name}; {moment(proposal.tmEventData.dates.start.dateTime).format('MMM Do YYYY, h:mm a')}
                                    </List.Description>
                                </List.Content>
                            </List.Item>
                        }
                    </List>
                </Item.Description>
                <Item.Extra>
                    <Label.Group>
                        {finalized
                            ? <Label size='tiny' content='finalized' color='blue' />
                            : <Label size='tiny' content='in progress' color='green' />}
                    </Label.Group>
                </Item.Extra>
            </Item.Content>
        </Item>
    );
};

export default ProposalListItem;